package com.tongminhnhut.logindemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.facebook.Profile
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val intent = intent.extras[MainActivity.PROFILE] as Profile
        val name = intent.name
        val idUser = intent.id
        val imgUrl = "https://graph.facebook.com/" + idUser + "/picture?type=large"

        txtName.text = name
        Glide.with(this).load(imgUrl).into(imgAvatar)

    }
}
