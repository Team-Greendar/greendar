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
import com.example.greendar.data.model.ResponseProfileImage
import com.example.greendar.data.model.ResponseRegisterUser
import com.example.greendar.data.recycler.UserInfo
import com.example.greendar.databinding.ActivityProfileSettingBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File


class ProfileSettingActivity:AppCompatActivity() {

    private lateinit var binding: ActivityProfileSettingBinding
    private lateinit var launcher:ActivityResultLauncher<Intent>

    //check flag
    private var nameFlag = false
    private var filePath = ""  //파일 경로
    private var fileAddress = "EMPTY"  //절대 경로

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityProfileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnNext.isEnabled = false
        binding.textInputEditTextUsername.addTextChangedListener(nameListener)

        init()
        //여기 입니다....(1)
        binding.btnCamera.setOnClickListener {
            when {
                //1. 처음 부터 허용 권한 있었음
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "move to Album", Toast.LENGTH_SHORT).show()
                    navigatePhoto()
                }
                //2.
                //권한을 명시적 으로 거부한 경우 true
                //처음 보거나, 다시 묻지 않음을 선택한 경우 false
                //교육용 팝업 확인 후 권한 팝업을 띄우는 기능
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) -> {
                    showPermissionContextPopup()
                }
                //3. 처음 으로 앱을 실행 하고 앨범 접근할 때 실행 되는 코드
                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }
            }
        }

        binding.btnNext.setOnClickListener {
            val username = binding.textInputEditTextUsername.text.toString()
            val message = binding.textInputEditTextStatusMessage.text.toString()

            //구글 회원 가입
            if (intent.getStringExtra("provider") == "com.google") {
                val googleEmail = intent.getStringExtra("googleEmail").toString()
                val googlePassword = intent.getStringExtra("googlePassword").toString()
                val googleUid = intent.getStringExtra("googleUid").toString()

                val postRegister = PostRegisterUser(
                    googleEmail,
                    googleUid,
                    username,
                    googlePassword,
                    fileAddress,
                    message
                )
                Log.d("Yuri", "imageUrl : $fileAddress, message: $message")

                postUserInfo(postRegister, googleUid)
            }
            //일반 회원 가입
            else {
                val email = intent.getStringExtra("email").toString()
                val password = intent.getStringExtra("password").toString()
                val uid = intent.getStringExtra("uid").toString()

                val postRegister =
                    PostRegisterUser(email, uid, username, password, fileAddress, message)
                Log.d("Yuri", "imageUrl : $fileAddress, message: $message")
                postUserInfo(postRegister, uid)
            }
        }
    }


    //API - image multipart
    private fun postImage(body : MultipartBody.Part){
        RetrofitAPI.post.postSendProfileImage(body)
            .enqueue(object : retrofit2.Callback<ResponseProfileImage> {
                override fun onResponse(
                    call: Call<ResponseProfileImage>,
                    response: Response<ResponseProfileImage>
                ) {
                    if (response.body()?.header?.status == 500) {
                        Log.e("Yuri", "서버 로직 에러")
                    } else if (response.body()?.header?.status == 405) {
                        Log.e("Yuri", "파일 형식 혹은 파일 parameter 의 이름 오류 혹은 파일 없음")
                    } else if (response.body()?.header?.status == 200) {
                        Log.e("Yuri", "서버 연결 and response 성공")
                        Log.e("Yuri", "서버 에서 받아온 주소 : ${response.body()?.body!!}")
                        fileAddress = response.body()?.body!!
                        Log.d("Yuri", "절대 경로 : $fileAddress")
                    } else {
                        Log.e("Yuri", "sth wrong...! OMG")
                        Log.e("Yuri", "error : ${response.body()?.header?.status}")
                        Log.e("Yuri", "error : ${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseProfileImage>, t: Throwable) {
                    Log.e("Yuri", "연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //API - send user info
    private fun postUserInfo(postRegister:PostRegisterUser, token:String){
        RetrofitAPI.post.postRegisterUser(postRegister)
            .enqueue(object:retrofit2.Callback<ResponseRegisterUser>{
                override fun onResponse(
                    call: Call<ResponseRegisterUser>,
                    response: Response<ResponseRegisterUser>
                ) {
                    if(response.body()?.header?.status == 200){
                        if(response.body()?.header?.message == "SUCCESS"){
                            Log.e("Yuri", "로그인 성공")
                            //todo : intent로 값 넘길 건지, 변수 저장 할 건지 고민
                            UserInfo.token = token

                            //다음 으로 넘어감
                            Log.e("Yuri", "move to Calendar")
                            //Log.e("Yuri", "token : $token")
                            val intent = Intent(this@ProfileSettingActivity, CalendarActivity::class.java)
                            //intent.putExtra("token", token)
                            startActivity(intent)
                        }
                        else{
                            Log.e("Yuri", "이미 존재 하는 닉네임")
                            Toast.makeText(this@ProfileSettingActivity, "This name already exists.\nPlease enter a new name", Toast.LENGTH_SHORT).show()
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

    //이미지 절대 경로 찾기
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


    private fun init(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                val intent = checkNotNull(result.data)
                val imageUri = intent.data

                imageUri.let{
                    Glide.with(this)
                        .load(imageUri)
                        .into(binding.btnProfile)
                }
                Log.d("Yuri", "imageUri : $imageUri")

                //절대 경로
                filePath = getRealPathFromURI(imageUri!!)

                //todo
                val file = File(filePath)
                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

                Log.d("Yuri", "이미지 경로 : $filePath")
                Log.d("Yuri", "body : $body")
                postImage(body)  //여기서 파일 경로 획득
            }
        }
    }


    //갤러리 에서 사진 가져 오기
    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/jpeg"
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