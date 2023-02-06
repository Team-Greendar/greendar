package com.example.greendar

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityStartBinding

class StartActivity : AppCompatActivity(){

    private lateinit var binding:ActivityStartBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //바인딩
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Register Activity 연결
        binding.btnRegister.setOnClickListener {
            startActivity(Intent(this@StartActivity, RegisterActivity::class.java))
        }

        //Login Activity 연결
        binding.btnLogin.setOnClickListener {
            //startActivity(Intent(this@StartActivity, LoginActivity::class.java))
            startActivity(Intent(this@StartActivity, ProfileSettingActivity::class.java))
        }
    }

}