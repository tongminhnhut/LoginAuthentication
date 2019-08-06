package com.tongminhnhut.logindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.facebook.appevents.AppEventsLogger
import kotlinx.android.synthetic.main.activity_main.*
import com.facebook.login.LoginResult
import com.facebook.login.LoginManager
import android.content.Intent
import android.telecom.Call
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.facebook.*
import java.util.*
import org.json.JSONException
import com.facebook.Profile.getCurrentProfile
import com.facebook.internal.ImageRequest.getProfilePictureUri
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import android.R.attr.data
import android.app.PendingIntent.getActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
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
        // [END config_signin]

        googleSignInClient = GoogleSignIn.getClient(this, gso)


        initLoginFacebook()

        initLoginGoogle()


//        btnFacebook.setOnClickListener {
//            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"))
//            LoginManager.getInstance().registerCallback(callbackManager,
//                object : FacebookCallback<LoginResult> {
//                    override fun onSuccess(loginResult: LoginResult) {
//                        Toast.makeText(this@MainActivity, "Login success", Toast.LENGTH_SHORT).show()
//                        btnnLogoutFacebook.visibility = View.VISIBLE
//                        btnFacebook.visibility = View.GONE
//                        if (loginResult.accessToken != null && !loginResult.accessToken.isExpired){
//                            val profile = Profile.getCurrentProfile()
//                            if (profile != null){
//                                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
//                                intent.putExtra(PROFILE, profile)
//                                startActivity(intent)
////                                setDataLogin(profile)
//
//                            }
//                        }
//
//                    }
//
//                    override fun onCancel() {
//                        // App code
//                    }
//
//                    override fun onError(exception: FacebookException) {
//                        // App code
//                        Toast.makeText(this@MainActivity, "Login error:" + exception, Toast.LENGTH_SHORT).show()
//
//                    }
//                })
//        }

//        initLoginGoogle()

        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired


        btnnLogoutFacebook.setOnClickListener {
           LoginManager.getInstance().logOut()
            btnnLogoutFacebook.visibility = View.GONE
            btnFacebook.visibility = View.VISIBLE
        }


        btnLogoutGoogle.setOnClickListener {
            googleSignInClient.signOut()
//            firebaseAuth.signOut()
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
            signIn()
        }
    }

    private fun initLoginFacebook() {

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email","public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        handleFacebookAccessToken(loginResult.accessToken)
//                        Toast.makeText(this@MainActivity, "Login success", Toast.LENGTH_SHORT).show()
                        btnnLogoutFacebook.visibility = View.VISIBLE
                        btnFacebook.visibility = View.INVISIBLE
//                        if (loginResult.accessToken != null && !loginResult.accessToken.isExpired){
//                            val profile = Profile.getCurrentProfile()
//                            if (profile != null){
//                                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
//                                intent.putExtra(PROFILE, profile)
//                                startActivity(intent)
////                                setDataLogin(profile)
//
//                            }
//                        }

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
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LLLLLLLLL", "signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                Toast.makeText(this@MainActivity, "Login success:" + user?.displayName, Toast.LENGTH_SHORT).show()
                } else {
                    Log.w("LLLLLLLLL", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }

                // ...
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
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("PPPPPPPPP", "signInWithCredential:success")
                    val user = firebaseAuth.currentUser?.displayName
                    Toast.makeText(this@MainActivity, "Login google: "+user, Toast.LENGTH_SHORT).show()
                    btnGoogle.visibility = View.INVISIBLE
                    btnLogoutGoogle.visibility = View.VISIBLE

                } else {
                    Log.w("PPPPP", "signInWithCredential:failure", task.exception)
                }

            }
    }


    companion object {
        const val PROFILE = "profile"
        const val RC_SIGN_IN = 1000
    }
}
