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
import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.ResponseRegisterUser
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Response
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
            Log.d("Yuri", "email:$email, password:$password")

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this){task->
                    //정보 있음
                    if(task.isSuccessful){
                        if(checkAuth()){
                            //이메일 인증 까지 완료
                            //token O -> 메인 페이지, token X -> profile_setting
                            findUser(auth.currentUser?.uid.toString(), "normal")
                        } else{
                            //이메일 인증을 완료 하지 않음
                            Toast.makeText(this, "e-mail authentication is not completed.\nPlease check your e-mail", Toast.LENGTH_SHORT).show()
                        }
                    }
                    //회원 정보 없음
                    else{
                        Toast.makeText(this, "Log in failed.\nPlease check your e-mail and password again.", Toast.LENGTH_SHORT).show()
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
                    .addOnCompleteListener(this){task ->
                        if(task.isSuccessful){
                            //구글 로그인 성공
                            //token O -> 메인 페이지, token X -> register_password
                            Log.e("Yuri", "로그인 성공")
                            findUser(auth.currentUser?.uid.toString(), "com.google")
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
            Log.e("Yuri", "google log in check") //kk
        }
    }

    //회원 있니? api
    private fun findUser(userToken:String, provider:String){
        RetrofitAPI.post.postFindUser(userToken)
            .enqueue(object:retrofit2.Callback<ResponseRegisterUser>{
                override fun onResponse(
                    call: Call<ResponseRegisterUser>,
                    response: Response<ResponseRegisterUser>
                ) {
                    if((response.body()?.header?.status == 200)) {
                        //토큰 존재 : move to Calendar
                        //todo : 토큰으로 보낼지, 변수로 저장할지.. (만나서 고민)
                        token = userToken
                        Log.d("Yuri", "log in success")
                        startActivity(Intent(this@LoginActivity, CalendarActivity::class.java))

                    }else if((response.body()?.header?.status == 500)){
                        //토큰 존재 X
                        if(provider == "com.google"){
                            //구글
                            Log.d("Yuri", "googleEmail : ${auth.currentUser?.email.toString()}")
                            Log.d("Yuri", "googleUid : $userToken")
                            Log.d("Yuri", "provider : ${auth.currentUser!!.providerData[0].toString().substring(0,10)}")

                            val intent = Intent(this@LoginActivity, RegisterPasswordActivity::class.java)
                            //우선 이름 같게
                            intent.putExtra("googleEmail", auth.currentUser?.email.toString())
                            intent.putExtra("googleUid", userToken)
                            intent.putExtra("provider", auth.currentUser!!.providerData[0].toString().substring(0,10))
                            startActivity(intent)
                        }
                        else{
                            //일반
                            Log.d("Yuri", "email: ${binding.textInputEditTextEmail.text.toString()}")
                            Log.d("Yuri", "password : ${binding.textInputEditTextPassword.text.toString()}")
                            Log.d("Yuri", "uid: $userToken")

                            val intent = Intent(this@LoginActivity, ProfileSettingActivity::class.java)
                            //우선 이름 같게
                            intent.putExtra("email", binding.textInputEditTextEmail.text.toString())
                            intent.putExtra("password",binding.textInputEditTextPassword.text.toString())
                            intent.putExtra("uid", userToken)
                            startActivity(intent)
                        }
                    }
                    else{
                        Log.d("Yuri", "sth wrong")
                    }
                }
                override fun onFailure(call: Call<ResponseRegisterUser>, t: Throwable) {
                    Log.e("Yuri", "서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
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

    //이메일 인증 했는지 확인
    private fun checkAuth():Boolean{
        val currentUser = auth.currentUser
        return currentUser?.let{
            currentUser.isEmailVerified
        } ?: let{
            false
        }
    }
}