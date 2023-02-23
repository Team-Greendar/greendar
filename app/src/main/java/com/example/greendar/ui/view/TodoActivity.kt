package com.example.greendar.ui.view

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import com.example.greendar.data.recycler.DailyAdapter
import com.example.greendar.data.recycler.DailyTodo
import com.example.greendar.databinding.ActivityTodoBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class TodoActivity: AppCompatActivity() {
    private lateinit var binding:ActivityTodoBinding

    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var filePath = ""

    //recyclerView 가 불러올 목록
    private var dailyadapter: DailyAdapter? = null
    private var dailydata:MutableList<DailyTodo> = mutableListOf()

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
        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //여기서 오류 생김
        //supportActionBar?.setDisplayShowTitleEnabled(true)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        initialize()  //data 값 초기화
        dailyadapter = DailyAdapter()
        dailyadapter!!.listData = dailydata
        binding.recyclerViewDailyTodo.adapter = dailyadapter
        binding.recyclerViewDailyTodo.layoutManager = LinearLayoutManager(this)

        init()

        //to-do 4 : to-do 추가
        binding.dailyTodo.setOnClickListener {
            dailydata.add(DailyTodo("", false, true))
            dailyadapter?.notifyItemInserted(dailydata.size -1)
        }
    }

    //TODO : api 에서 값 받아 와서 초기 설정
    private fun initialize(){
        with(dailydata){
            add(DailyTodo("use tumbler", true, false))
            add(DailyTodo("recycle plastic bottle", false, false))
            add(DailyTodo("work out", true, false))
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
            dailydata[position].modifyTodoFlag = true
            dailyadapter?.notifyDataSetChanged()

        }

        //투두 삭제
        val delete = bottomSheetDialog.findViewById<Button>(R.id.btn_delete_todo)
        delete?.setOnClickListener {
            bottomSheetDialog.dismiss()

            deleteTodo(member)
        }

        //TODO 3 : 이미지 추가, 삭제
        //TODO : 이미지 선택 후, 이미지 uri를 dailydata리스트에 저장, dailyadapter?.notifyDataSetChanged() 추가.
        //TODO : 이미지 boolean = false -> 버튼 = add photo,  이미지 boolean = true -> 버튼 = delete photo
        val image = bottomSheetDialog.findViewById<Button>(R.id.btn_verify_photo)
        image?.setOnClickListener {
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

        bottomSheetDialog.show()
    }

    fun deleteTodo(member:DailyTodo){
        dailydata.remove(member)
        dailyadapter?.notifyDataSetChanged()
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
        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intent = checkNotNull(result.data)
                val imageUri = intent.data

                imageUri.let{
                    Glide.with(this)
                        .load(imageUri)

                }
                filePath = getRealPathFromURI(imageUri!!)
            }
        }
    }


    //갤러리 에서 사진 가져 오기
    private fun navigatePhoto(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
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