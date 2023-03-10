package com.example.greendar.ui.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
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
import com.example.greendar.data.recycler.UserInfo.default_Address
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.data.recycler.UserInfo.username
import com.example.greendar.databinding.ActivityTodoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

class TodoActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTodoBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var filePath = ""
    private var publicPosition = 0
    private var whichTodo = 0

    //recyclerView ??? ????????? ??????
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

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)

        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = ""


        //daily to-do ?????? ?????????
        getDailyTodoInfo(token, date)
        //event to-do ?????? ?????????
        getEventTodoInfo(token, date)

        //activity ??? recycler ??????
        dailyAdapter = DailyAdapter()
        dailyAdapter!!.listData = dailyData
        binding.recyclerViewDailyTodo.adapter = dailyAdapter
        binding.recyclerViewDailyTodo.layoutManager = LinearLayoutManager(this)

        eventAdapter = EventAdapter()
        eventAdapter!!.listData = eventData
        binding.recyclerViewEventTodo.adapter = eventAdapter
        binding.recyclerViewEventTodo.layoutManager = LinearLayoutManager(this)

        init()

        //todo ??? split : 2023-01-01
        val splitDate = date.split("-")
        Log.d("Yuri", "$splitDate")

        //todo ??? ??????
        val monthEnglish = when(splitDate[1]){
            "01" -> "January"
            "02" -> "February"
            "03" -> "March"
            "04" -> "April"
            "05" -> "May"
            "06" -> "June"
            "07" -> "July"
            "08" -> "August"
            "09" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> "Month not Found"
        }

        //todo ?????? ??????
        val localDate = LocalDate.of(splitDate[0].toInt(), splitDate[1].toInt(), splitDate[2].toInt())
        val dayOfWeek = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.US)

        binding.year.text = splitDate[0]
        binding.month.text = monthEnglish
        binding.day.text = splitDate[2]
        binding.dayOfWeek.text = dayOfWeek


        //to-do 4 : to-do ??????
        binding.dailyTodo.setOnClickListener {
            dailyData.add(DailyTodo(false, date, "EMPTY", username, 0, "", true))
            dailyAdapter?.notifyItemInserted(dailyData.size -1)
        }

    }

    //daily ?????? ????????? ??????
    private fun getDailyTodoInfo(token:String, date:String){
        RetrofitAPI.getDaily.getDailyTodo(token, date)
            .enqueue(object:Callback<GetDailyTodo>{
                override fun onResponse(
                    call: Call<GetDailyTodo>,
                    response: Response<GetDailyTodo>
                ) {
                    if(response.code() == 200){
                        Log.e("Yuri", "????????? ?????? ??? ?????? ???")
                        addDailyTodo(response.body())
                    } else{
                        Log.e("Yuri", "????????? ?????? sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<GetDailyTodo>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //event ?????? ????????? ??????
    private fun getEventTodoInfo(token:String, date:String){
        RetrofitAPI.getEvent.getEventTodo(token, date)
            .enqueue(object:Callback<GetEventTodo>{
                override fun onResponse(
                    call: Call<GetEventTodo>,
                    response: Response<GetEventTodo>
                ) {
                    if(response.code() == 200){
                        Log.e("Yuri", "Event Todo ??? ?????? ???")
                        addEventTodo(response.body())
                    } else{
                        Log.e("Yuri", "????????? ?????? sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<GetEventTodo>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //daily ?????? ????????? ?????? ??????
    private fun addDailyTodo(searchResult:GetDailyTodo?){
        dailyData.clear()
        if(!searchResult?.body.isNullOrEmpty()){
            //daily to-do ?????????
            for(document in searchResult!!.body){
                //????????? recycler View ??? ??????
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
            //to-do ??????
            Log.d("Yuri", "????????? ?????? ?????? ??????")
        }
    }

    //event ?????? ????????? ?????? ??????
    private fun addEventTodo(searchResult:GetEventTodo?){
        eventData.clear()
        if(!searchResult?.body.isNullOrEmpty()){
            //event to-do ?????????
            for(document in searchResult!!.body){
                //????????? recycler View ??? ??????
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
            //to-do ??????
            Log.d("Yuri", "????????? ?????? ?????? ??????")
        }
    }

    //todo : ????????? ?????? ?????? -> ????????? ?????? ????????? ??????, task, date ?????? ?????? ??????.
    fun showImageBottomSheetDialog(imageUrl: String, task:String, date:String){
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_show_image)
        bottomSheetDialog.behavior.state = STATE_EXPANDED

        val image = bottomSheetDialog.findViewById<ImageView>(R.id.iv_image)
        val tvTask = bottomSheetDialog.findViewById<TextView>(R.id.tv_task)
        val tvDate = bottomSheetDialog.findViewById<TextView>(R.id.tv_date)
        val exit = bottomSheetDialog.findViewById<ImageButton>(R.id.btn_exit)
        val popTop = bottomSheetDialog.findViewById<ConstraintLayout>(R.id.pop_top)
        val popBottom = bottomSheetDialog.findViewById<ConstraintLayout>(R.id.pop_bottom)

        //????????? ?????? ??????
        //var path = "https://images.unsplash.com/photo-1661956602868-6ae368943878?ixlib=rb-4.0.3&ixid=MnwxMjA3fDF8MHxlZGl0b3JpYWwtZmVlZHwxfHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=1200&q=60"
        //path = "https://images.unsplash.com/photo-1677840147160-6545dba6f08d?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=687&q=80"

        var path = default_Address + imageUrl
        tvTask?.text = task
        tvDate?.text = date

        Glide.with(image!!)
            .load(path)
            .listener(object: RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d("Yuri", "image success")
                    return false
                }
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("Yuri", "fail : ${e.toString()}")
                    return false
                }
            })
            .error(R.drawable.ic_leaf)
            .into(image)

        image.setOnClickListener{
            if((popTop?.visibility == View.VISIBLE) and (popBottom?.visibility == View.VISIBLE)){
                exit?.setOnClickListener  {
                    bottomSheetDialog.dismiss()
                }
                popTop?.visibility = View.INVISIBLE
                popBottom?.visibility = View.INVISIBLE
            }else if((popTop?.visibility == View.INVISIBLE) and (popBottom?.visibility == View.INVISIBLE)){
                popTop?.visibility = View.VISIBLE
                popBottom?.visibility = View.VISIBLE
            }
        }

        bottomSheetDialog.show()
    }

    //Event to do (?????? ??????)
    fun showEventBottomSheetDialog(member:EventTodo, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_event_todo)

        //?????? ?????? ????????? ??????
        val todoText = bottomSheetDialog.findViewById<TextView>(R.id.tv_todo_text)
        todoText!!.text = eventData[position].task

        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_upload_photo)
        if(eventData[position].imageUrl == "EMPTY"){
            image?.setText(R.string.upload_todo)
        }else{
            image?.setText(R.string.delete_photo)
        }

        //????????? ??????, ??????
        image?.setOnClickListener {
            bottomSheetDialog.dismiss()
            publicPosition = position
            if(eventData[position].imageUrl == "EMPTY") {
                //?????? ??????
                whichTodo = 1
                checkPermission()
                eventAdapter?.notifyItemChanged(position)
            }else{
                //?????? ??????
                eventData[position].imageUrl = "EMPTY"
                deleteEventTodoImage(token, eventData[position].event_todo_id, position)
                eventAdapter?.notifyItemChanged(position)
            }
        }

        bottomSheetDialog.show()
    }

    //Daily to do (????????? ??????)
    fun showDailyBottomSheetDialog(member:DailyTodo, position:Int) {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_daily_todo)

        //?????? ?????? ????????? ??????
        val todoText = bottomSheetDialog.findViewById<TextView>(R.id.tv_todo_text)
        todoText!!.text = dailyData[position].task

        //?????? ??????
        val modify = bottomSheetDialog.findViewById<Button>(R.id.btn_modify_todo)
        modify?.setOnClickListener {
            bottomSheetDialog.dismiss()

            dailyData[position].modifyClicked = true
            dailyAdapter?.notifyItemChanged(position)

        }

        //?????? ??????
        val delete = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_todo)
        delete?.setOnClickListener {
            bottomSheetDialog.dismiss()
            Log.e("Yuri", "?????? ?????? ??????")
            Log.e("Yuri", "todo id : ${dailyData[position].private_todo_id}")

            deleteDailyTodo(token, dailyData[position].private_todo_id)
            deleteTodo(member)
        }

        //????????? ??????, ??????
        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_photo)
        if(dailyData[position].imageUrl == "EMPTY"){
            image?.setText(R.string.upload_todo)
        }else{
            image?.setText(R.string.delete_photo)
        }

        //TO-DO 3 : ????????? ??????, ??????
        //TO-DO : ????????? ?????? ???, ????????? uri ??? dailyData ????????? ??? ??????, dailyAdapter?.notifyDataSetChanged() ??????.
        image?.setOnClickListener {
            bottomSheetDialog.dismiss()
            publicPosition = position
            if(dailyData[position].imageUrl == "EMPTY") {
                //?????? ??????
                whichTodo = 2
                checkPermission()
                dailyAdapter?.notifyItemChanged(position)
            }else{
                //?????? ??????
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

    //(api - ??????) to-do : ?????? ??????
    fun deleteDailyTodo(token:String, todoId:Int){
        RetrofitAPI.getDaily.deleteDailyTodo(token, todoId)
            .enqueue(object:Callback<ResponseDeleteDailyTodo>{
                override fun onResponse(
                    call: Call<ResponseDeleteDailyTodo>,
                    response: Response<ResponseDeleteDailyTodo>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "?????? ?????? : ?????? ?????? ??????")
                        Log.e("Yuri", "?????? ?????? : ${response.body()!!.body}")
                    } else {
                        Log.e("Yuri", "?????? ?????? ?????? : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDeleteDailyTodo>, t: Throwable) {
                    Log.e("Yuri", "?????? ?????? ??????")
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
        Log.d("Yuri", "?????? ?????? : $filePath")

        val file = File(filePath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestBody)

        //????????? or ????????? ?????? ?????? ??????... (????????? ??????)
        when(whichTodo){
            1 -> {
                //????????? ??????
                Log.d("Yuri", "????????? ?????? ????????? ??????")
                putEventImage(token, body, eventData[position].event_todo_id, position)
                whichTodo = 0
            }
            2 ->{
                //?????????(private) ??????
                Log.d("Yuri", "????????? ?????? ????????? ??????")
                putDailyImage(token, body, dailyData[position].private_todo_id, position)
                whichTodo = 0
            }
            else ->{
                Log.e("Yuri", "????????? : ????????? ?????? ????????? ?????? ?????? ?????? : $whichTodo")
            }
        }

    }

    //????????? ?????? ?????? ??????
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

    //????????? ?????? ?????? ?????? ??????
    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_PICK)  //(jpeg : Intent.ACTION_PICK, file : ACTION_GET_CONTENT)
        intent.type = "image/jpeg"
        launcher.launch(intent)
    }

    //(api - ??????) to-do : daily to-do ????????? ??????
    private fun putDailyImage(token:String, body:MultipartBody.Part, id:Int, position:Int){
        RetrofitAPI.getDaily.putDailyImage(token, body, id)
            .enqueue(object:Callback<ResponseDailyNewTodo>{
                override fun onResponse(
                    call: Call<ResponseDailyNewTodo>,
                    response: Response<ResponseDailyNewTodo>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "????????? ?????? ????????? ?????? ?????? : ?????? ?????? ??????")
                        Log.e("Yuri", "task : ${response.body()!!.body.task}")
                        Log.e("Yuri", "imageUrl : ${response.body()!!.body.imageUrl}")
                        dailyData[position].imageUrl = response.body()!!.body.imageUrl
                        //dailyAdapter?.notifyDataSetChanged()
                        dailyAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "????????? ?????? ????????? ?????? ?????? : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDailyNewTodo>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - ??????) to-do : event to-do ????????? ??????
    private fun putEventImage(token:String, body:MultipartBody.Part, id:Int, position:Int){
        RetrofitAPI.getEvent.putEventImage(token, body, id)
            .enqueue(object:Callback<ResponseEventTodoComplete>{
                override fun onResponse(
                    call: Call<ResponseEventTodoComplete>,
                    response: Response<ResponseEventTodoComplete>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "????????? ?????? ????????? ?????? ?????? : ?????? ?????? ??????")
                        Log.e("Yuri", "task : ${response.body()!!.body.task}")
                        Log.e("Yuri", "imageUrl : ${response.body()!!.body.imageUrl}")
                        eventData[position].imageUrl = response.body()!!.body.imageUrl
                        //dailyAdapter?.notifyDataSetChanged()
                        eventAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "????????? ?????? ????????? ?????? ?????? : sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseEventTodoComplete>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - ??????) to-do : daily to-do ????????? ??????
    private fun deleteDailyTodoImage(token:String, id:Int, position: Int){
        Log.d("Yuri", "token : $token")
        Log.d("Yuri", "id : $id")
        RetrofitAPI.getDaily.deleteDailyTodoImage(token, id)
            .enqueue(object:Callback<ResponseDailyTodoImage>{
                override fun onResponse(
                    call: Call<ResponseDailyTodoImage>,
                    response: Response<ResponseDailyTodoImage>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "????????? ?????? ?????? ??????: ?????? ?????? ??????")
                        Log.e("Yuri", "message : ${response.body()!!.header.message}")
                        dailyData[position].imageUrl = "EMPTY"
                        //dailyAdapter?.notifyDataSetChanged()
                        dailyAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "????????? ?????? ????????? ??????: sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDailyTodoImage>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //(api - ??????) to-do : event to-do ????????? ??????
    private fun deleteEventTodoImage(token:String, id:Int, position: Int){
        RetrofitAPI.getEvent.deleteEventTodoImage(token, id)
            .enqueue(object:Callback<ResponseDeleteEventImage>{
                override fun onResponse(
                    call: Call<ResponseDeleteEventImage>,
                    response: Response<ResponseDeleteEventImage>
                ) {
                    if (response.code() == 200) {
                        Log.e("Yuri", "????????? ?????? ?????? ??????: ?????? ?????? ??????")
                        Log.e("Yuri", "message : ${response.body()!!.header.message}")
                        eventData[position].imageUrl = "EMPTY"
                        //dailyAdapter?.notifyDataSetChanged()
                        eventAdapter?.notifyItemChanged(position)
                    } else {
                        Log.e("Yuri", "????????? ?????? ????????? ??????: sth wrong..! OMG")
                        Log.e("Yuri", "${response.code()}")
                        Log.e("Yuri", "${response.body()?.header?.message}")
                    }
                }
                override fun onFailure(call: Call<ResponseDeleteEventImage>, t: Throwable) {
                    Log.e("Yuri", "????????? ?????? ?????? ?????? ?????? ?????? ??????")
                    Log.e("Yuri", t.toString())
                }
            })
    }

    //UserPermission 1
    private fun checkPermission(){
        when {
            //1. ?????? ?????? ?????? ?????? ?????????
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                navigatePhoto()
            }
            //2. ????????? ?????? ?????? ??? ?????? ????????? ????????? ??????
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) -> {
                showPermissionContextPopup()
            }
            //3. ?????? ?????? ?????? ?????? ?????? ?????? ????????? ??? ?????? ?????? ??????
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
                    //????????? ?????? ??????
                    //?????? ?????????
                    Toast.makeText(this, "move to Album", Toast.LENGTH_SHORT).show()
                    navigatePhoto()
                }
                else{
                    //?????? ?????????
                    Toast.makeText(this, "Album access denied", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //do nothing
            }
        }
    }

    //todo : keyboard
    /*
    fun keyboardDown(view:View){
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }*/

    //appBar goBack
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                //?????? ?????? ?????? ????????? ???
                //return ??? boolean ??????
                finish()
                return true
            }
            else->{}
        }
        return super.onOptionsItemSelected(item)
    }

}