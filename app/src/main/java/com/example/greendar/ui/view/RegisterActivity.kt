package com.example.greendar.ui.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.R
import com.example.greendar.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity:AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth:FirebaseAuth

    private var emailFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)

        binding.btnBack.setOnClickListener{
            startActivity(Intent(this@RegisterActivity, StartActivity::class.java))
        }

        //일반 회원 가입 하기 - 이메일 정보 가지고 다음 페이지 로 넘어감
        binding.btnRegister.setOnClickListener {
            val username = intent.getStringExtra("username").toString()
            Log.d("Yuri", "Normal Register : Send E-mail : ${binding.textInputEditTextEmail.text.toString()}")
            Log.d("Yuri", "Username : $username")

            val intent = Intent(this, RegisterPasswordActivity::class.java)
            intent.putExtra("normalEmail", binding.textInputEditTextEmail.text.toString())
            intent.putExtra("username", username)
            startActivity(Intent(this@RegisterActivity, RegisterPasswordActivity::class.java))
            startActivity(intent)
        }

        auth=Firebase.auth
        //Google 로그인 결과 처리
        val requestLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        )
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try{
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this){task->
                        if(task.isSuccessful){
                            //구글 로그인 성공
                            Toast.makeText(this, "register complete", Toast.LENGTH_SHORT).show()
                            //TODO: 이메일 정보 가지고 다음 으로 넘어 가기
                            Log.d("Yuri", "Google Register : Send E-mail and Provider")
                            val intent = Intent(this, RegisterPasswordActivity::class.java)
                            intent.putExtra("googleEmail", auth.currentUser?.email.toString())
                            Log.d("Yuri", "Google e-mail : ${auth.currentUser?.email}")
                            Log.d("Yuri", "Provider : ${auth.currentUser!!.providerData[0]}")
                            Log.d("Yuri", "User Uid : ${auth.currentUser?.uid}")
                            startActivity(Intent(this@RegisterActivity, RegisterPasswordActivity::class.java))
                        } else{
                            //구글 로그인 실패
                            Toast.makeText(this, "register failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch(e : ApiException){
                //예외 처리
            }
        }

        binding.btnRegisterGoogle.setOnClickListener {
            //구글 회원 가입  하기 - firebase 로 인증 진행
            val gso = GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build()
            val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
            requestLauncher.launch(signInIntent)
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
        binding.btnRegister.isEnabled = emailFlag
    }

}