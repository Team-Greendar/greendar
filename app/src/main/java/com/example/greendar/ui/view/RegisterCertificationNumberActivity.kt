package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityRegisterCertificationNumberBinding

class RegisterCertificationNumberActivity:AppCompatActivity() {
    private lateinit var binding:ActivityRegisterCertificationNumberBinding

    //check flag
    private var certiFlag = false

    //여기 코드 api 받아서 해와야 합니당.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityRegisterCertificationNumberBinding.inflate(layoutInflater)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@RegisterCertificationNumberActivity, RegisterActivity::class.java))
        }

    }

    private fun flagCheck(){
        binding.btnSetPassword.isEnabled = certiFlag
    }



}