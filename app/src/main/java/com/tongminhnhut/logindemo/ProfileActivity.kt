package com.tongminhnhut.logindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val firebaseUser = intent.extras[MainActivity.PROFILE] as FirebaseUser
        val urlPhoto = intent.extras[MainActivity.URL_PHOTO] as String
        val name = firebaseUser.displayName
        val email = firebaseUser.email
        val idUser  = firebaseUser.providerData.get(1).uid

        val urlimg = "https://graph.facebook.com/" + idUser+ "/picture?type=large"
        txtName.text = name
        txtEmail.text = email
        Glide.with(this).load(urlPhoto).into(imgAvatar)

    }
}
