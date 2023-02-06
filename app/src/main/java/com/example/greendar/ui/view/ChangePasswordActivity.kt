package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity:AppCompatActivity() {

    private lateinit var binding:ActivityChangePasswordBinding

    //check flag
    private var passwordFlag = false
    private var passwordConfirmFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)
        binding.textInputEditTextPasswordConfirm.addTextChangedListener(passwordConfirmListener)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@ChangePasswordActivity, LoginActivity::class.java))
        }
    }



    //password check 해서 error 띄움
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

    //password double check
    private val passwordConfirmListener=object:TextWatcher{
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

    //password check 해서 error 띄움
    fun flagCheck(){
        binding.btnSubmit.isEnabled = (passwordFlag && passwordConfirmFlag)
    }
}