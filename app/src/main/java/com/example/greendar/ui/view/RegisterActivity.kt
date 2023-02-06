package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityRegisterBinding

class RegisterActivity:AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //check flag
    private var emailFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@RegisterActivity, StartActivity::class.java))
        }

    }

    //e-mail check (login 재사용)
    private val emailListener = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if(s != null){
                when{
                    s.isEmpty() ->{
                        binding.textInputLayoutEmail.error = "Please enter you e-mail"
                        emailFlag=false
                    }
                    !LoginActivity().emailRegex(s.toString()) ->{
                        binding.textInputLayoutEmail.error = "e-mail format is incorrect"
                        emailFlag = false
                    }
                    else->{
                        binding.textInputLayoutEmail.error = null
                        emailFlag = true
                    }
                }
                flagCheck()
            }
        }
    }

    private fun flagCheck(){
        binding.btnSendNumber.isEnabled = emailFlag
    }

}