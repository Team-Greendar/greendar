package com.example.greendar.ui.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.R
import com.example.greendar.databinding.ActivityTodoBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class TodoActivity:AppCompatActivity() {
    private lateinit var binding:ActivityTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //여기서 오류 생김
        //supportActionBar?.setDisplayShowTitleEnabled(true)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding.btnBottomSheet.setOnClickListener {
            showEventBottomSheetDialog()
            //showDailyBottomSheetDialog()
        }
    }

    //Event to do (고정 투두)
    private fun showEventBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_event_todo)

        val dismiss = bottomSheetDialog.findViewById<Button>(R.id.btn_verify_photo)
        
        dismiss?.setOnClickListener {
            Log.d("Yuri", "dismiss")
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    //Daily to do (사용자 투두)
    private fun showDailyBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_daily_todo)


        bottomSheetDialog.show()
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