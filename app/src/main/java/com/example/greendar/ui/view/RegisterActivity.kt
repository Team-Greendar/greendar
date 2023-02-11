package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity:AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth:FirebaseAuth

    //check flag
    private var emailFlag = false
    private var passwordFlag = false
    private var passwordConfirmFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)
        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)
        binding.textInputEditTextPasswordConfirm.addTextChangedListener(passwordConfirmListener)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@RegisterActivity, StartActivity::class.java))
        }


        auth = Firebase.auth
        binding.btnRegister.setOnClickListener {
            //회원 가입 하기
            val email = binding.textInputEditTextEmail.text.toString()
            val password = binding.textInputEditTextPassword.text.toString()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task ->
                    if(task.isSuccessful){
                        //계정 등록 성공
                        auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if(sendTask.isSuccessful){
                                    //인증 메일 발송 성공
                                    Toast.makeText(this, "인증 메일을 확인해야 회원가입이 완료 됩니다.\n이메일 확인해주고, 로그인 해서 Greendar를 사용해주세요", Toast.LENGTH_SHORT).show()
                                    //TODO: 이메일 인증이 완료 되었 으면, 로그인 창으로 이동
                                } else{
                                    //인증 메일 발송 실패
                                    Toast.makeText(this, "인증 메일 발송 실패. 이메일 주소를 다시 확인해주세요. ", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else{
                        //계정 등록 실패
                        Log.w("kkang", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(this, "회원 가입 실패", Toast.LENGTH_SHORT).show()
                    }
                }
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

    //password check (changePassword 재활용)
    private val passwordListener = object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if(s != null){
                when{
                    s.isEmpty()->{
                        binding.textInputLayoutPassword.error = "please enter your password"
                        passwordFlag=false
                    }
                    !LoginActivity().passwordRegex(s.toString())->{
                        binding.textInputLayoutPassword.error = "English uppercase/lowercase letters, numbers (max 8 letters)"
                        passwordFlag = false
                        when{
                            binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString() ->{
                                binding.textInputLayoutPasswordConfirm.error = "wrong password"
                                passwordConfirmFlag = false
                            }
                            else ->{
                                binding.textInputLayoutPasswordConfirm.error = null
                                passwordConfirmFlag = true
                            }
                        }
                    }
                    else->{
                        binding.textInputLayoutPassword.error=null
                        passwordFlag=true
                        when{
                            binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString() ->{
                                binding.textInputLayoutPasswordConfirm.error = "wrong password"
                                passwordConfirmFlag = false
                            }
                            else ->{
                                binding.textInputLayoutPasswordConfirm.error = null
                                passwordConfirmFlag = true
                            }
                        }
                    }
                }
                flagCheck()
            }
        }
    }

    //password confirm check (changePassword 재활용)
    private val passwordConfirmListener=object: TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if(s!=null){
                if(s.isEmpty()){
                    binding.textInputLayoutPasswordConfirm.error = "please enter your password"
                    passwordConfirmFlag = false
                }

                else if(binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString()){
                    binding.textInputLayoutPasswordConfirm.error = "wrong password"
                    passwordConfirmFlag = false
                }

                else{
                    binding.textInputLayoutPasswordConfirm.error = null
                    passwordConfirmFlag = true
                }
                flagCheck()
            }
        }
    }

    private fun flagCheck(){
        binding.btnRegister.isEnabled = (emailFlag && passwordFlag && passwordConfirmFlag)
    }

}