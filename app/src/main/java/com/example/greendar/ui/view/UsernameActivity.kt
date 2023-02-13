package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityUsernameBinding

class UsernameActivity:AppCompatActivity() {
    private lateinit var binding:ActivityUsernameBinding

    //check flag
    private var nameFlag = false

    //여기 코드 api 받아서 해와야 합니당.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityUsernameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextUsername.addTextChangedListener(nameListener)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@UsernameActivity, StartActivity::class.java))
        }

        binding.btnRegister.setOnClickListener {
            val username:String = binding.textInputEditTextUsername.text.toString()
            Log.d("Yuri", "Send Username : $username")

            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("username", username)

            startActivity(Intent(this@UsernameActivity, RegisterActivity::class.java))
            startActivity(intent)
        }
    }

    private val nameListener = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if(s != null){
                when{
                    s.isEmpty() ->{
                        binding.textInputLayoutUsername.error = "please enter your name"
                        nameFlag = false
                    }
                    else ->{
                        binding.textInputLayoutUsername.error = null
                        nameFlag = true
                    }
                }
                flagCheck()
            }
        }
    }

    private fun flagCheck(){
        binding.btnRegister.isEnabled = nameFlag
    }

}