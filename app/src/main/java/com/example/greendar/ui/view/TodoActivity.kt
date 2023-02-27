package com.example.greendar.ui.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.greendar.R

import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.GetDailyTodo
import com.example.greendar.data.recycler.DailyAdapter
import com.example.greendar.data.recycler.DailyTodo
import com.example.greendar.data.recycler.UserInfo.date
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.data.recycler.UserInfo.username
import com.example.greendar.databinding.ActivityTodoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import retrofit2.Call
import retrofit2.Response

class TodoActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTodoBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var filePath = ""
    var publicPosition = 0

    //recyclerView 가 불러올 목록

    private var dailyAdapter: DailyAdapter? = null
    private var dailyData:MutableList<DailyTodo> = mutableListOf()



    init{
        instance = this
    }

    companion object{
        private var instance:TodoActivity? = null
        fun getInstance():TodoActivity?{
            return instance
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //여기서 오류 생김
        //supportActionBar?.setDisplayShowTitleEnabled(true)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        //TODO : GET api info 연결 함수 작성
        getDailyTodoInfo(token, date)

        dailyAdapter = DailyAdapter()
        dailyAdapter!!.listData = dailyData
        binding.recyclerViewDailyTodo.adapter = dailyAdapter

        binding.recyclerViewDailyTodo.layoutManager = LinearLayoutManager(this)

        init()

        //to-do 4 : to-do 추가
        binding.dailyTodo.setOnClickListener {

            dailyData.add(DailyTodo(false, date, "EMPTY", username, 0, "", true))
            dailyAdapter?.notifyItemInserted(dailyData.size -1)
        }
    }

    //api 에서 값 받아 와서 초기 설정 (연결 성공)
    private fun getDailyTodoInfo(token:String, date:String){
        RetrofitAPI.getDaily.getDailyTodo(token, date)
            .enqueue(object:retrofit2.Callback<GetDailyTodo>{
                override fun onResponse(
                    call: Call<GetDailyTodo>,
                    response: Response<GetDailyTodo>
                ) {
                    if(response.code() == 200){
                        Log.e("Yuri", "값 전달 됨")
                        addDailyTodo(response.body())
                    } else{
                        Log.e("Yuri", "sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<GetDailyTodo>, t: Throwable) {
                    Log.e("Yuri", "서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //리스트 에 받아온 정보 연결
    private fun addDailyTodo(searchResult:GetDailyTodo?){
        dailyData.clear()
        if(!searchResult?.body.isNullOrEmpty()){
            //daily to-do 존재함
            for(document in searchResult!!.body){
                //결과를 recycler View 에 추가
                val todo = DailyTodo(
                    document.complete,
                    document.date,
                    document.imageUrl,
                    document.name,
                    document.private_todo_id,
                    document.task,
                    false
                )
                dailyData.add(todo)
                username = document.name
                Log.d("Yuri", "task : ${document.task}")
            }
            Log.d("Yuri", "Username : $username")
            dailyAdapter?.notifyDataSetChanged()
        }else{
            //to-do 없음
            Log.d("Yuri", "결과 없음")
        }
    }

    //Event to do (고정 투두)
    fun showEventBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_event_todo)
        //TODO 1 : 사진 추가, 삭제


        bottomSheetDialog.show()
    }

    //Daily to do (사용자 투두)
    fun showDailyBottomSheetDialog(member:DailyTodo, position:Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_daily_todo)

        //투두 수정
        val modify = bottomSheetDialog.findViewById<Button>(R.id.btn_modify_todo)
        modify?.setOnClickListener {
            bottomSheetDialog.dismiss()

            dailyData[position].modifyClicked = true
            dailyAdapter?.notifyItemChanged(position)

        }

        //투두 삭제
        val delete = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_todo)
        delete?.setOnClickListener {
            bottomSheetDialog.dismiss()

            deleteTodo(member)
        }


        //이미지 추가, 삭제
        //TODO : 서버에 요청 -> 이미지 없을 때 값 통일
        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_photo)
        if(dailyData[position].imageUrl == "EMPTY"){
            image?.setText(R.string.upload_todo)
        }else{
            image?.setText(R.string.delete_photo)
        }

        //TODO 3 : 이미지 추가, 삭제
        //TODO : 이미지 선택 후, 이미지 uri를 dailydata리스트에 저장, dailyadapter?.notifyDataSetChanged() 추가.
        //TODO : 이미지 boolean = false -> 버튼 = add photo,  이미지 boolean = true -> 버튼 = delete photo
        image?.setOnClickListener {
            bottomSheetDialog.dismiss()
            publicPosition = position
            if(dailyData[position].imageUrl == "EMPTY") {
                checkPermission()
            }else{
                //사진 삭제
                dailyData[position].imageUrl = "EMPTY"
                //TODO : 여기서 서버 연결

                dailyAdapter?.notifyItemChanged(position)
            }
        }
        bottomSheetDialog.show()
    }

    fun deleteTodo(member:DailyTodo){
        dailyData.remove(member)
        dailyAdapter?.notifyDataSetChanged()
    }

    private fun init(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                filePath = getRealPathFromURI(imageUri!!)  //서버에 보내는 uri

                uploadPhoto(publicPosition, imageUri.toString())  //todo : parameter에 filepath 추가 해서 서버 연결할 때 사용
            }
        }
    }

    fun uploadPhoto(position:Int, imageUrl:String) {
        dailyData[position].imageUrl = imageUrl
        //TODO 여기서 서버 연결 : position 활용 해서 정보 찾아서 연결

        dailyAdapter?.notifyDataSetChanged()
        Log.e("Yuri", imageUrl)
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

    //갤러리 에서 사진 가져 오기
    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        launcher.launch(intent)
    }

    //UserPermission 1
    private fun checkPermission(){
        when {
            //1. 처음 부터 허용 권한 있었음
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigatePhoto()
            }
            //2. 교육용 팝업 확인 후 권한 팝업을 띄우는 기능
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


    //User Permission 2
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

    //UserPermission 3
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

    /*
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                //뒤로 가기 버튼 눌렀을 때
                //return 은 boolean 으로
                return false
            }
        }
        return false  //이거는 임시로 작성한 코드
    }
    */
}