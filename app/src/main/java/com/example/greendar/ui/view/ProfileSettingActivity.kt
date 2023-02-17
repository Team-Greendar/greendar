package com.example.greendar.ui.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import com.bumptech.glide.Glide
import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.PostRegisterUser
import com.example.greendar.data.model.ResponseRegisterUser
import com.example.greendar.databinding.ActivityProfileSettingBinding
import retrofit2.Call
import retrofit2.Response


class ProfileSettingActivity:AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingBinding
    private lateinit var launcher:ActivityResultLauncher<Intent>

    //check flag
    private var nameFlag = false
    private var imageFile = "EMPTY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityProfileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.isEnabled = false

        init()
        binding.textInputEditTextUsername.addTextChangedListener(nameListener)

        //여기 입니다....(1)
        binding.btnCamera.setOnClickListener{
            when{
                //1. 처음 부터 허용 권한 있었음
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )== PackageManager.PERMISSION_GRANTED ->{
                    Toast.makeText(this, "move to Album", Toast.LENGTH_SHORT).show()
                    navigatePhoto()
                }
                //2.
                //권한을 명시적 으로 거부한 경우 true
                //처음 보거나, 다시 묻지 않음을 선택한 경우 false
                //교육용 팝업 확인 후 권한 팝업을 띄우는 기능
                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ->{
                    showPermissionContextPopup()
                }
                //3. 처음 으로 앱을 실행 하고 앨범 접근할 때 실행 되는 코드
                else -> {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }

        binding.btnNext.setOnClickListener {
            val username = binding.textInputEditTextUsername.text.toString()
            val message = binding.textInputEditTextStatusMessage.text.toString()

            //구글 회원 가입
            if(intent.getStringExtra("provider") == "com.google"){
                val googleEmail = intent.getStringExtra("googleEmail").toString()
                val googlePassword = intent.getStringExtra("googlePassword").toString()
                val googleUid = intent.getStringExtra("googleUid").toString()

                val postRegister = PostRegisterUser(googleEmail, googleUid, username, googlePassword, imageFile, message)
                Log.d("Yuri", "imageUrl : $imageFile, message: $message")
                postUserInfo(postRegister)
            }
            //일반 회원 가입
            else{
                val email = intent.getStringExtra("email").toString()
                val password = intent.getStringExtra("password").toString()
                val uid = intent.getStringExtra("uid").toString()

                val postRegister = PostRegisterUser(email, uid, username, password, imageFile, message)
                Log.d("Yuri", "imageUrl : $imageFile, message: $message")
                postUserInfo(postRegister)
            }
        }
    }

    //API
    private fun postUserInfo(postRegister:PostRegisterUser){
        RetrofitAPI.post.postRegisterUser(postRegister)
            .enqueue(object:retrofit2.Callback<ResponseRegisterUser>{
                override fun onResponse(
                    call: Call<ResponseRegisterUser>,
                    response: Response<ResponseRegisterUser>
                ) {
                    if(response.body()?.header?.status == 200){
                        if(response.body()?.header?.message == "SUCCESS"){
                            Log.e("Yuri", "로그인 성공")
                            Toast.makeText(this@ProfileSettingActivity, "회원가입 성공. 다음으로 넘어감", Toast.LENGTH_SHORT).show()
                            //TODO 다음 으로 넘어감
                        }
                        else{
                            Log.e("Yuri", "이미 존재하는 닉네임이다")
                            Toast.makeText(this@ProfileSettingActivity, "이미 존재하는 username입니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
                override fun onFailure(
                    call: Call<ResponseRegisterUser>,
                    t: Throwable
                ) {
                    Log.e("Yuri", "연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    private fun init(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                imageUri?.let{
                    //서버 업로드 위해 이미지 절대 경로 변환
                    imageFile = getRealPathFromURI(it)

                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.btnProfile)
                }
            }
        }
    }

    private fun getRealPathFromURI(uri: Uri):String{
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursorLoader = CursorLoader(this, uri, proj, null, null, null)
        val cursor: Cursor? = cursorLoader.loadInBackground()

        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val url: String = cursor.getString(columnIndex)
        cursor.close()
        return url
    }

    //갤러리 에서 사진 가져 오기
    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        launcher.launch(intent)
    }


    //여기 입니다....(2)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("Need Permission")
            .setMessage("Greendar requires permission to select photos.")
            .setPositiveButton("Agree", {_, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            })
            .setNegativeButton("Deny", {_, _->})
            .create()
            .show()
    }

    //여기 입니다....(3)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1000 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //권한이 허용 된것
                    //허용 클릭시
                    Toast.makeText(this, "move to Album", Toast.LENGTH_SHORT).show()
                    navigatePhoto()
                }
                else{
                    //거부 클릭시
                    Toast.makeText(this, "Album access denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //do nothing
            }
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
        binding.btnNext.isEnabled = nameFlag
    }
}