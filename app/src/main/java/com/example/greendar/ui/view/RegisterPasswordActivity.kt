package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityRegisterPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/*
TODO : 일반 회원 가입 - 비밀 번호 받고 인증 메일 보내기, 인증 해야 다음 페이지 로 넘어갈 수 있다.
TODO : 구글 회원 가입 - 비밀 번호 받고 계정 관리로 넘어갈 수 있다.
*/

class RegisterPasswordActivity:AppCompatActivity() {
    private lateinit var binding: ActivityRegisterPasswordBinding
    private lateinit var auth: FirebaseAuth

    //check flag
    private var passwordFlag = false
    private var passwordConfirmFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityRegisterPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)
        binding.textInputEditTextPasswordConfirm.addTextChangedListener(passwordConfirmListener)

        //일반 회원 가입
        if(intent.getStringExtra("googleEmail") == null){
            binding.tvEmail.text = intent.getStringExtra("normalEmail")
            Log.d("Yuri", "normal email : ${intent.getStringExtra("normalEmail")}")
            Log.d("Yuri", "google email : ${intent.getStringExtra("googleEmail")}")
        }
        //구글 회원 가입
        else if(intent.getStringExtra("normalEmail") == null){
            binding.tvEmail.text = intent.getStringExtra("googleEmail")
            Log.d("Yuri", "normal email : ${intent.getStringExtra("normalEmail")}")
            Log.d("Yuri", "google email : ${intent.getStringExtra("googleEmail")}")
        }

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@RegisterPasswordActivity, RegisterActivity::class.java))
        }

        //일반 회원 가입 - 인증 이메일 전송 필요 - 인증 끝나면 로그인 까지 자동 완료 해주고 계정 설정 으로 넘어감
        //구글 회원 가입 완료 - 그냥 넘어감
        auth = Firebase.auth
        binding.btnRegister.setOnClickListener {
            val email = binding.tvEmail.text.toString()
            val password = binding.textInputEditTextPassword.text.toString()

            //구글 회원 가입
            if (intent.getStringExtra("provider") == "com.google") {
                val googleUid = intent.getStringExtra("googleUid").toString()
                val provider = intent.getStringExtra("provider").toString()

                Log.d("Yuri", "googleEmail : $email")
                Log.d("Yuri", "googlePassword : $password")
                Log.d("Yuri", "googleUid : $googleUid")

                val intent = Intent(this, ProfileSettingActivity::class.java)
                intent.putExtra("googleEmail", email)
                intent.putExtra("googlePassword", password)
                intent.putExtra("googleUid", googleUid)
                intent.putExtra("provider", provider)
                startActivity(intent)
            }
            //일반 회원 가입
            else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            //계정 등록 성공
                            auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { sendTask ->
                                    if (sendTask.isSuccessful) {
                                        //인증 메일 발송 성공
                                        Toast.makeText(
                                            this,
                                            "인증 메일을 확인 해야 회원가입이 완료 됩니다.\n이메일 확인 해주고, 로그인 해서 Greendar를 사용해주세요",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        //인증 메일 발송 실패
                                        Toast.makeText(
                                            this,
                                            "인증 메일 발송 실패. 이메일 주소를 다시 확인해주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                        //계정 등록 실패(이미 계정이 등록 되어 있는 상태) -> 이메일 인증 완료
                        else if (checkAuth()) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    //계정 일치
                                    if (task.isSuccessful) {
                                        Log.d("Yuri", "이메일 인증 O, 계정 일치 O")
                                        Log.d(
                                            "Yuri",
                                            "email : ${binding.tvEmail.text}, password : ${binding.textInputEditTextPassword.text.toString()}"
                                        )
                                        Log.d(
                                            "Yuri",
                                            "uid: ${auth.currentUser?.uid.toString()}"
                                        )

                                        val intent =
                                            Intent(this, ProfileSettingActivity::class.java)
                                        intent.putExtra("email", binding.tvEmail.text)
                                        intent.putExtra(
                                            "password",
                                            binding.textInputEditTextPassword.text.toString()
                                        )
                                        intent.putExtra("uid", auth.currentUser?.uid.toString())

                                        startActivity(intent)
                                    }
                                    //계정 일치 X
                                    else {
                                        Log.d("Yuri", "이메일 인증 O, 비밀 번호 일치 X")
                                        Toast.makeText(
                                            this,
                                            "비밀번호를 다시 확인 해주세요",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                        }
                        //계정 등록 실패(이미 계정이 등록 되어 있는 상태) -> 이메일 인증 완료 X
                        else if (!checkAuth()) {
                            Log.d("Yuri", "이메일 인증 X")
                            Toast.makeText(this, "이메일 인증을 다시 해주 세요.", Toast.LENGTH_SHORT).show()
                        } else {
                            //계정 등록 실패 : 다양한 이유가 있지만 그 중 하나 이미 있는 계정 등
                            Log.w("Yuri", "createUserWithEmail:failure", task.exception)
                            Toast.makeText(this, "회원 가입 실패", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    //이메일 인증 했는지 check
    private fun checkAuth(): Boolean {
        val currentUser = auth.currentUser
        return currentUser?.let {
            currentUser.isEmailVerified
        } ?: let {
            false
        }
    }



    //password check (changePassword 재활용)
    private val passwordListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                when {
                    s.isEmpty() -> {
                        binding.textInputLayoutPassword.error = "please enter your password"
                        passwordFlag = false
                    }
                    !LoginActivity().passwordRegex(s.toString()) -> {
                        binding.textInputLayoutPassword.error =
                            "English uppercase/lowercase letters, numbers (max 8 letters)"
                        passwordFlag = false
                        when {
                            binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString() -> {
                                binding.textInputLayoutPasswordConfirm.error =
                                    "wrong password"
                                passwordConfirmFlag = false
                            }
                            else -> {
                                binding.textInputLayoutPasswordConfirm.error = null
                                passwordConfirmFlag = true
                            }
                        }
                    }
                    else -> {
                        binding.textInputLayoutPassword.error = null
                        passwordFlag = true
                        when {
                            binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString() -> {
                                binding.textInputLayoutPasswordConfirm.error =
                                    "wrong password"
                                passwordConfirmFlag = false
                            }
                            else -> {
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
    private val passwordConfirmListener = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(s: Editable?) {
            if (s != null) {
                if (s.isEmpty()) {
                    binding.textInputLayoutPasswordConfirm.error =
                        "please enter your password"
                    passwordConfirmFlag = false
                } else if (binding.textInputEditTextPasswordConfirm.text.toString() != binding.textInputEditTextPassword.text.toString()) {
                    binding.textInputLayoutPasswordConfirm.error = "wrong password"
                    passwordConfirmFlag = false
                } else {
                    binding.textInputLayoutPasswordConfirm.error = null
                    passwordConfirmFlag = true
                }
                flagCheck()
            }
        }
    }

    private fun flagCheck() {
        binding.btnRegister.isEnabled = (passwordFlag && passwordConfirmFlag)
    }

}