package com.example.greendar.ui.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.greendar.databinding.ActivityTodoBinding

class TodoActivity:AppCompatActivity() {
    private lateinit var binding:ActivityTodoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)


        binding.imageButton1.setOnClickListener {

        }
    }

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


}