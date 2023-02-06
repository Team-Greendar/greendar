package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityLoginBinding
import java.util.regex.Pattern

class LoginActivity:AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //ckeck flag
    private var emailFlag = false
    private var passwordFlag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)
        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@LoginActivity, StartActivity::class.java))
        }

    }

    //이메일 제약 조건(이메일 주소가 맞는지)
    fun emailRegex(string: String):Boolean{
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(string).matches()){
            return true
        }
        return false
    }

    //이메일 check 해서 error 띄움
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
                    !emailRegex(s.toString()) ->{
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

    //비밀번호 제약 조건(영어&숫자, 8자리 까지)
    fun passwordRegex(string:String):Boolean{
        if(Pattern.matches("^[a-zA-Z\\d]{1,8}$", string)){
            return true
        }
        return false
    }

    //비밀번호 check해서 error 띄움
    private val passwordListener = object:TextWatcher{
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if(s != null){
                when{
                    s.isEmpty()->{
                        binding.textInputLayoutPassword.error = "please enter your password"
                        passwordFlag=false
                    }
                    !passwordRegex(s.toString())->{
                        binding.textInputLayoutPassword.error = "English uppercase/lowercase letters, numbers (max 8 letters)"
                        passwordFlag=false
                    }
                    else->{
                        binding.textInputLayoutPassword.error=null
                        passwordFlag=true
                    }
                }
                flagCheck()
            }
        }
    }


    //비밀 번호 check 해서 error 띄움
    fun flagCheck(){
        binding.btnLogin.isEnabled = (emailFlag && passwordFlag)
    }

    /*서버 check
    val postUser = PostModel(1, "sore")
    private fun postUserInfo(postUser: PostModel){
        RetrofitAPI.post.postUsers(postUser)
            .enqueue(object:retrofit2.Callback<PostResult>{
                override fun onResponse(call: Call<PostResult>, response: Response<PostResult>) {

                }
            })
    }
    */

}