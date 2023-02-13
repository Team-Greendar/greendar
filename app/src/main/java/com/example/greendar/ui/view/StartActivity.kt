package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity(){

    private lateinit var binding:ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        //바인딩
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Username Activity 연결
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this@StartActivity, UsernameActivity::class.java))
        }

        //Login Activity 연결
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this@StartActivity, LoginActivity::class.java))
        }
    }

}