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
            //이미 존재 하는 이메일 check
            auth.fetchSignInMethodsForEmail(binding.textInputEditTextEmail.text.toString())
                .addOnCompleteListener(this) { task ->
                    val isNewUser = task.result.signInMethods?.isEmpty()
                    if (isNewUser!!) {
                        //새로운 유저
                        Log.d("Yuri", "Is New User!")
                        Log.d("Yuri", "Normal e-mail : ${binding.textInputEditTextEmail.text.toString()}")
                        val intent = Intent(this, RegisterPasswordActivity::class.java)
                        intent.putExtra("normalEmail", binding.textInputEditTextEmail.text.toString())
                        startActivity(intent)
                    } else {
                        //있는 유저
                        Log.d("Yuri", "이미 존재 하는 이메일")
                        Toast.makeText(this, "이미 존재 하는 이메일 입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
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
                            //구글 회원 가입 성공
                            Toast.makeText(this, "register complete", Toast.LENGTH_SHORT).show()
                            //TODO: 이메일 정보 가지고 다음 으로 넘어 가기
                            Log.d("Yuri", "googleEmail : ${auth.currentUser?.email}")
                            Log.d("Yuri", "provider : ${auth.currentUser!!.providerData[0]}")
                            Log.d("Yuri", "googleUid : ${auth.currentUser?.uid}")

                            val intent = Intent(this, RegisterPasswordActivity::class.java)
                            intent.putExtra("googleEmail", auth.currentUser?.email.toString())
                            intent.putExtra("googleUid", auth.currentUser?.uid.toString())
                            intent.putExtra("provider", auth.currentUser!!.providerData[0].toString().substring(0,10))
                            startActivity(intent)
                        } else{
                            //구글 회원 가입 실패
                            Toast.makeText(this, "register failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch(e : ApiException){
                //예외 처리
            }
        }

        binding.btnRegisterGoogle.setOnClickListener {
            //구글 회원 가입  하기 - firebase 로 인증 진행
            //이미 존재 하는 이메일 check
            auth.fetchSignInMethodsForEmail(auth.currentUser?.email.toString())
                .addOnCompleteListener(this) { task ->
                    val isNewUser = task.result.signInMethods?.isEmpty()
                    if (isNewUser!!) {
                        Log.d("Yuri", "Is New User!")
                        //새로운 이메일
                        val gso = GoogleSignInOptions
                            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken("923471445103-g69s7l8gp5qjh8k3phhb6fqnsojoee16.apps.googleusercontent.com")
                            .requestEmail()
                            .requestProfile()
                            .build()
                        val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent
                        requestLauncher.launch(signInIntent)
                    } else {
                        Log.d("Yuri", "Is Old User!")
                        Toast.makeText(this, "이미 존재 하는 이메일 입니다. 로그인 진행해 주세요", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    //이메일 존재 하는지 안 하는지 check
    private fun existEmail(email: String){
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener(this) { task ->
                val isNewUser = task.result.signInMethods?.isEmpty()
                if (isNewUser!!) {
                    Log.d("Yuri", "Is New User!")

                } else {
                    Log.d("Yuri", "Is Old User!")

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

    private fun flagCheck(){
        binding.btnRegister.isEnabled = emailFlag
    }

}