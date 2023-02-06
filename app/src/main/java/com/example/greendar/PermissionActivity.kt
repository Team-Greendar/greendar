package com.example.greendar

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.greendar.databinding.ActivityPermissionBinding


class PermissionActivity:AppCompatActivity() {

    private lateinit var binding: ActivityPermissionBinding

    //onCreate()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityPermissionBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //여기 입니다....(1)
        binding.btnConfirm.setOnClickListener{
            when{
                //1. 처음부터 허용 권한 있었음
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                )==PackageManager.PERMISSION_GRANTED ->{
                    Toast.makeText(this, "권한 있음, 사진첩으로 고고", Toast.LENGTH_SHORT).show()
                    //navigatePhoto()
                }
                //2.
                //권한을 명시적으로 거부한 경우 true
                //처음보거나, 다시묻지 않음을 선택한 경우 false
                //교육용 팝업 확인 후 권한 팝업을 띄우는 기능
                ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) ->{
                    showPermissionContextPopup()
                }
                //3. 처음으로 앱을 실행하고 앨범 접근할 때 실행되는 코드
                else -> {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }

            }
    }

    //여기 입니다....(2)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("Greendar에서 사진을 선택하러면 권한이 필요합니다.")
            .setPositiveButton("동의하기", {_, _ ->
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
            })
            .setNegativeButton("취소하기", {_, _->})
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
                    Toast.makeText(this, "권한 있음, 사진첩으로 고고", Toast.LENGTH_SHORT).show()
                    //navigatePhoto()
                }
                else{
                    //거부 클릭시
                    Toast.makeText(this, "권한을 거부했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                //do nothing
            }
        }
    }

}
