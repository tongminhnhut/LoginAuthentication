package com.tongminhnhut.logindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import android.content.Intent
import android.util.Log
import android.view.View
import com.facebook.*
import java.util.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


class MainActivity : AppCompatActivity() {
    var callbackManager : CallbackManager? = null
    lateinit var firebaseAuth : FirebaseAuth
    // [END declare_auth]

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        firebaseAuth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(this, gso)


        initLoginFacebook()

        initLoginGoogle()



        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired


        btnnLogoutFacebook.setOnClickListener {
           LoginManager.getInstance().logOut()
            btnnLogoutFacebook.visibility = View.GONE
            btnFacebook.visibility = View.VISIBLE
        }


        btnLogoutGoogle.setOnClickListener {
            googleSignInClient.signOut()
            btnGoogle.visibility = View.VISIBLE
            btnLogoutGoogle.visibility = View.INVISIBLE
        }



    }
    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun initLoginGoogle() {
        btnGoogle.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            signIn()
        }
    }

    private fun initLoginFacebook() {

        btnFacebook.setOnClickListener {
            progress_bar.visibility = View.VISIBLE
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        handleFacebookAccessToken(loginResult.accessToken)
                        btnnLogoutFacebook.visibility = View.VISIBLE
                        btnFacebook.visibility = View.INVISIBLE

                    }

                    override fun onCancel() {
                        // App code
                    }

                    override fun onError(exception: FacebookException) {
                        // App code
                        Toast.makeText(this@MainActivity, "Login error:" + exception, Toast.LENGTH_SHORT).show()

                    }
                })
        }

    }

    private fun handleFacebookAccessToken(accessToken: AccessToken?) {
        val credential = FacebookAuthProvider.getCredential(accessToken!!.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    val acct = task.result?.user
                    val photUrl = acct?.getPhotoUrl()!!.toString()
                    val user = firebaseAuth.currentUser
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra(PROFILE, user)
                    intent.putExtra(URL_PHOTO, photUrl)
                    startActivity(intent)

                Toast.makeText(this@MainActivity, "Login success:" + user?.displayName, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w("PPPPP", "Google sign in failed", e)
            }
        }

    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progress_bar.visibility = View.GONE
                if (task.isSuccessful) {
                    val acct = task.result?.user
                    val photUrl = acct?.getPhotoUrl()!!.toString()
                    val user = firebaseAuth.currentUser
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    intent.putExtra(URL_PHOTO, photUrl)
                    intent.putExtra(PROFILE, user)
                    startActivity(intent)


                    Toast.makeText(this@MainActivity, "Login google: "+ user?.displayName, Toast.LENGTH_SHORT).show()
                    btnGoogle.visibility = View.INVISIBLE
                    btnLogoutGoogle.visibility = View.VISIBLE

                } else {
                    Log.w("PPPPP", "signInWithCredential:failure", task.exception)
                }

            }
    }


    companion object {
        const val URL_PHOTO ="url-photo"
        const val PROFILE = "profile"
        const val RC_SIGN_IN = 1000
    }
}
