package com.example.greendar

import android.os.Bundle
import android.os.PersistableBundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityRegisterBinding

class RegisterActivity:AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //check flag
    private var emailFlag = false
    private var passwordFlag = false
    private var passwordConfirmFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)
        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)
        binding.textInputEditTextPasswordConfirm.addTextChangedListener(passwordConfirmListener)

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

    fun flagCheck(){
        binding.btnRegister.isEnabled = (emailFlag && passwordFlag && passwordConfirmFlag)
    }

}