package com.example.greendar.ui.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
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
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.greendar.R
import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.*
import com.example.greendar.data.recycler.DailyAdapter
import com.example.greendar.data.recycler.DailyTodo
import com.example.greendar.data.recycler.EventAdapter
import com.example.greendar.data.recycler.EventTodo
import com.example.greendar.data.recycler.UserInfo.date
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.data.recycler.UserInfo.username
import com.example.greendar.databinding.ActivityTodoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class TodoActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTodoBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var filePath = ""
    var publicPosition = 0
    private var whichTodo = 0

    //recyclerView 가 불러올 목록
    private var dailyAdapter: DailyAdapter? = null
    private var dailyData:MutableList<DailyTodo> = mutableListOf()
    private var eventAdapter :EventAdapter? = null
    private var eventData:MutableList<EventTodo> = mutableListOf()

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


        //daily to-do 정보 불러옴
        getDailyTodoInfo(token, date)
        //event to-do 정보 불러옴
        getEventTodoInfo(token, date)

        //activity 에 recycler 연결
        dailyAdapter = DailyAdapter(this)
        dailyAdapter!!.listData = dailyData
        binding.recyclerViewDailyTodo.adapter = dailyAdapter
        binding.recyclerViewDailyTodo.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventAdapter()
        eventAdapter!!.listData = eventData
        binding.recyclerViewEventTodo.adapter = eventAdapter
        binding.recyclerViewEventTodo.layoutManager = LinearLayoutManager(this)

        init()

        //to-do 4 : to-do 추가
        binding.dailyTodo.setOnClickListener {
            dailyData.add(DailyTodo(false, date, "EMPTY", username, 0, "", true))
            dailyAdapter?.notifyItemInserted(dailyData.size -1)
        }

        //링크 확인
        /*
        binding.btnLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://storage.cloud.google.com/greendar_storage/test/Screenshot_20230222-182438_Greendar.jpg-haQc5L.jpg"))
            startActivity(intent)
        }*/


        //val path = "https://storage.cloud.google.com/greendar_storage/test/Screenshot_20230222-182438_Greendar.jpg-q0l5Td.jpg"

    }

    //daily 투두 일자별 검색
    private fun getDailyTodoInfo(token:String, date:String){
        RetrofitAPI.getDaily.getDailyTodo(token, date)
            .enqueue(object:retrofit2.Callback<GetDailyTodo>{
                override fun onResponse(
                    call: Call<GetDailyTodo>,
                    response: Response<GetDailyTodo>
                ) {
                    if(response.code() == 200){
                        Log.e("Yuri", "데일리 투두 값 전달 됨")
                        addDailyTodo(response.body())
                    } else{
                        Log.e("Yuri", "데일리 투두 sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<GetDailyTodo>, t: Throwable) {
                    Log.e("Yuri", "데일리 투두 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //event 투두 일자별 검색
    private fun getEventTodoInfo(token:String, date:String){
        RetrofitAPI.getEvent.getEventTodo(token, date)
            .enqueue(object:retrofit2.Callback<GetEventTodo>{
                override fun onResponse(
                    call: Call<GetEventTodo>,
                    response: Response<GetEventTodo>
                ) {
                    if(response.code() == 200){
                        Log.e("Yuri", "Event Todo 값 전달 됨")
                        addEventTodo(response.body())
                    } else{
                        Log.e("Yuri", "이벤트 투두 sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<GetEventTodo>, t: Throwable) {
                    Log.e("Yuri", "이벤트 투두 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //daily 투두 리스트 정보 연결
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
            Log.d("Yuri", "데일리 투두 결과 없음")
        }
    }

    //event 투두 리스트 정보 연결
    private fun addEventTodo(searchResult:GetEventTodo?){
        eventData.clear()
        if(!searchResult?.body.isNullOrEmpty()){
            //event to-do 존재함
            for(document in searchResult!!.body){
                //결과를 recycler View 에 추가
                val todo = EventTodo(
                    document.complete,
                    document.date,
                    document.imageUrl,
                    document.eventTodoItemId,
                    document.task
                )
                eventData.add(todo)
                Log.d("Yuri", "task: ${document.task}")
            }
            eventAdapter?.notifyDataSetChanged()
        }else{
            //to-do 없음
            Log.d("Yuri", "이벤트 투두 결과 없음")
        }
    }


    //Event to do (고정 투두)
    fun showEventBottomSheetDialog(member:EventTodo, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_event_todo)

        //바텀 시트 텍스트 연결
        val todoText = bottomSheetDialog.findViewById<TextView>(R.id.tv_todo_text)
        todoText!!.text = eventData[position].task


        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_upload_photo)
        if(eventData[position].imageUrl == "EMPTY"){
            image?.setText(R.string.upload_todo)
        }else{
            image?.setText(R.string.delete_photo)
        }

        //이미지 추가, 삭제
        image?.setOnClickListener {
            bottomSheetDialog.dismiss()
            publicPosition = position
            if(eventData[position].imageUrl == "EMPTY") {
                //사진 추가
                whichTodo = 1
                checkPermission()
                eventAdapter?.notifyItemChanged(position)
            }else{
                //사진 삭제
                eventData[position].imageUrl = "EMPTY"
                deleteEventTodoImage(token, eventData[position].event_todo_id, position)
                eventAdapter?.notifyItemChanged(position)
            }
        }

        bottomSheetDialog.show()
    }

    //Daily to do (사용자 투두)
    fun showDailyBottomSheetDialog(member:DailyTodo, position:Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_daily_todo)

        //바텀 시트 텍스트 연결
        val todoText = bottomSheetDialog.findViewById<TextView>(R.id.tv_todo_text)
        todoText!!.text = dailyData[position].task

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
            Log.e("Yuri", "삭제 버튼 누름")
            Log.e("Yuri", "todo id : ${dailyData[position].private_todo_id}")

            deleteDailyTodo(token, dailyData[position].private_todo_id)
            deleteTodo(member)
        }

        //이미지 추가, 삭제
        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_photo)
        if(dailyData[position].imageUrl == "EMPTY"){
            image?.setText(R.string.upload_todo)
        }else{
            image?.setText(R.string.delete_photo)
        }

        //TO-DO 3 : 이미지 추가, 삭제
        //TO-DO : 이미지 선택 후, 이미지 uri를 dailydata리스트에 저장, dailyadapter?.notifyDataSetChanged() 추가.
        image?.setOnClickListener {
            bottomSheetDialog.dismiss()
            publicPosition = position
            if(dailyData[position].imageUrl == "EMPTY") {
                //사진 추가
                whichTodo = 2
                checkPermission()
                dailyAdapter?.notifyItemChanged(position)
            }else{
                //사진 삭제
                dailyData[position].imageUrl = "EMPTY"
                deleteDailyTodoImage(token, dailyData[position].private_todo_id, position)
                dailyAdapter?.notifyItemChanged(position)
            }
        }
        bottomSheetDialog.show()
    }

    fun deleteTodo(member:DailyTodo){
        dailyData.remove(member)
        dailyAdapter?.notifyDataSetChanged()
    }

    //(api - 성공) to-do : 투두 삭제
    fun deleteDailyTodo(token:String, todoId:Int){
        RetrofitAPI.getDaily.deleteDailyTodo(token, todoId)
            .enqueue(object:retrofit2.Callback<ResponseDeleteDailyTodo>{
                override fun onResponse(
                    call: Call<ResponseDeleteDailyTodo>,
                    response: Response<ResponseDeleteDailyTodo>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "투두 삭제 : 서버 연결 성공")
                        Log.e("Yuri", "투두 삭제 : ${response.body()!!.body}")
                    } else {
                        Log.e("Yuri", "있는 투두 수정 : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDeleteDailyTodo>, t: Throwable) {
                    Log.e("Yuri", "서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    private fun init(){
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intent = checkNotNull(result.data)
                val imageUri = intent.data
                Log.d("Yuri", "imageUri : $imageUri")
                uploadPhoto(publicPosition, imageUri)
            }
        }
    }

    private fun uploadPhoto(position:Int, imageUrl:Uri?) {
        filePath = getRealPathFromURI(imageUrl!!)
        Log.d("Yuri", "절대 경로 : $filePath")

        val file = File(filePath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        //todo : 데일리 or 이벤트 어케 구분 하냐... (변수로 구분)
        when(whichTodo){
            1 -> {
                //이벤트 투두
                Log.d("Yuri", "이벤트 투두 이미지 연결")
                putEventImage(token, body, eventData[position].event_todo_id, position)
                whichTodo = 0
            }
            2 ->{
                //데일리(private) 투두
                Log.d("Yuri", "데일리 투두 이미지 연결")
                putDailyImage(token, body, dailyData[position].private_todo_id, position)
                whichTodo = 0
            }
            else ->{
                Log.e("Yuri", "이미지 : 데일리 인지 이벤트 인지 구분 오류 : $whichTodo")
            }
        }

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
        val intent = Intent(Intent.ACTION_PICK)  //(jpeg : Intent.ACTION_PICK, file : ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        launcher.launch(intent)
    }

    //(api - 성공) to-do : daily to-do 이미지 추가
    private fun putDailyImage(token:String, body:MultipartBody.Part, id:Int, position:Int){
        RetrofitAPI.getDaily.putDailyImage(token, body, id)
            .enqueue(object:retrofit2.Callback<ResponseDailyNewTodo>{
                override fun onResponse(
                    call: Call<ResponseDailyNewTodo>,
                    response: Response<ResponseDailyNewTodo>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "데일리 투두 이미지 추가 여부 : 서버 연결 성공")
                        Log.e("Yuri", "task : ${response.body()!!.body.task}")
                        Log.e("Yuri", "imageUrl : ${response.body()!!.body.imageUrl}")
                        dailyData[position].imageUrl = response.body()!!.body.imageUrl
                        //dailyAdapter?.notifyDataSetChanged()
                        dailyAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "데일리 투두 이미지 추가 여부 : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDailyNewTodo>, t: Throwable) {
                    Log.e("Yuri", "데일리 투두 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - 성공) to-do : event to-do 이미지 추가
    private fun putEventImage(token:String, body:MultipartBody.Part, id:Int, position:Int){
        RetrofitAPI.getEvent.putEventImage(token, body, id)
            .enqueue(object:Callback<ResponseEventTodoComplete>{
                override fun onResponse(
                    call: Call<ResponseEventTodoComplete>,
                    response: Response<ResponseEventTodoComplete>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "이벤트 투두 이미지 추가 여부 : 서버 연결 성공")
                        Log.e("Yuri", "task : ${response.body()!!.body.task}")
                        Log.e("Yuri", "imageUrl : ${response.body()!!.body.imageUrl}")
                        eventData[position].imageUrl = response.body()!!.body.imageUrl
                        //dailyAdapter?.notifyDataSetChanged()
                        eventAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "이벤트 투두 이미지 추가 여부 : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseEventTodoComplete>, t: Throwable) {
                    Log.e("Yuri", "이벤트 투두 사진 추가 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - 성공) to-do : daily to-do 이미지 삭제
    private fun deleteDailyTodoImage(token:String, id:Int, position: Int){
        Log.d("Yuri", "token : $token")
        Log.d("Yuri", "id : $id")
        RetrofitAPI.getDaily.deleteDailyTodoImage(token, id)
            .enqueue(object:retrofit2.Callback<ResponseDailyTodoImage>{
                override fun onResponse(
                    call: Call<ResponseDailyTodoImage>,
                    response: Response<ResponseDailyTodoImage>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "데일리 투두 사진 삭제: 서버 연결 성공")
                        Log.e("Yuri", "message : ${response.body()!!.header.message}")
                        dailyData[position].imageUrl = "EMPTY"
                        //dailyAdapter?.notifyDataSetChanged()
                        dailyAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "데일리 투두 이미지 삭제: sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDailyTodoImage>, t: Throwable) {
                    Log.e("Yuri", "데일리 투두 사진 삭제 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - 성공) to-do : event to-do 이미지 삭제
    private fun deleteEventTodoImage(token:String, id:Int, position: Int){
        RetrofitAPI.getEvent.deleteEventTodoImage(token, id)
            .enqueue(object:retrofit2.Callback<ResponseDeleteEventImage>{
                override fun onResponse(
                    call: Call<ResponseDeleteEventImage>,
                    response: Response<ResponseDeleteEventImage>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "이벤트 투두 사진 삭제: 서버 연결 성공")
                        Log.e("Yuri", "message : ${response.body()!!.header.message}")
                        eventData[position].imageUrl = "EMPTY"
                        //dailyAdapter?.notifyDataSetChanged()
                        eventAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "데일리 투두 이미지 삭제: sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDeleteEventImage>, t: Throwable) {
                    Log.e("Yuri", "이벤트 투두 사진 삭제 서버 연결 실패")
                    Log.e("Yuri", t.toString())
                }
            })
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