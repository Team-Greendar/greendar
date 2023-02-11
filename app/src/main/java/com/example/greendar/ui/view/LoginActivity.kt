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
import com.example.greendar.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.regex.Pattern

class LoginActivity:AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    //login
    private lateinit var auth:FirebaseAuth

    //check flag
    private var emailFlag = false
    private var passwordFlag = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textInputEditTextEmail.addTextChangedListener(emailListener)
        binding.textInputEditTextPassword.addTextChangedListener(passwordListener)

        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@LoginActivity, StartActivity::class.java))
        }

        auth= Firebase.auth
        //일반 로그인 진행
        binding.btnLogin.setOnClickListener {
            val email = binding.textInputEditTextEmail.text.toString()
            val password = binding.textInputEditTextPassword.text.toString()
            Log.d("kkang", "email:$email, password:$password")

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task->
                    //정보 있음
                    if(task.isSuccessful){
                        if(checkAuth()){
                            //user 가 이메일 인증 까지 완료 했을 때 (우리는 다음 페이지 로 넘어감)
                            //TODO
                            Toast.makeText(this, "log in complete", Toast.LENGTH_SHORT).show()
                        } else{
                            //이메일 인증을 완료하지 않음
                            Toast.makeText(this, "이메일 인증을 완료 하지 않음", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //회원 정보 없음
                    else{
                        Toast.makeText(this, "Log in failed. Please check your e-mail and password again.", Toast.LENGTH_SHORT).show()
                    }
                }
        }

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
                            Toast.makeText(this, "log in complete", Toast.LENGTH_SHORT).show()
                            //TODO: 다음 페이지 넘어 가는 것 구현
                            //startActivity(Intent(this@LoginActivity, StartActivity::class.java))
                        } else{
                            //구글 로그인 실패
                            Toast.makeText(this, "log in failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch(e : ApiException){
                //예외 처리
            }
        }

        //Google 로그인 진행
        binding.btnLoginGoogle.setOnClickListener {
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

    //비밀 번호 제약 조건(영어&숫자, 6~8자리 까지)
    fun passwordRegex(string:String):Boolean{
        if(Pattern.matches("^[a-zA-Z\\d]{6,8}$", string)){
            return true
        }
        return false
    }

    //비밀 번호 check 해서 error 띄움
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
                        binding.textInputLayoutPassword.error = "English uppercase/lowercase letters, numbers (min 6, max 8 letters)"
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

    //TODO : 코드 읽고 수정 필요
    private fun checkAuth():Boolean{
        val currentUser = auth.currentUser
        return currentUser?.let{
            //email = currentUser.email
            currentUser.isEmailVerified
        } ?: let{
            false
        }
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