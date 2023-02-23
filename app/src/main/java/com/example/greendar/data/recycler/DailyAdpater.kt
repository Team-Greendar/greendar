package com.example.greendar.data.recycler

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.greendar.R
import com.example.greendar.databinding.ItemTodoListBinding
import com.example.greendar.ui.view.TodoActivity


//RecyclerView 의 Adapter
class DailyAdapter:RecyclerView.Adapter<DailyAdapter.Holder>() {

    var listData = mutableListOf<DailyTodo>()


    //몇 개의 목록을 만들지 반환
    override fun getItemCount(): Int {
        return listData.size
    }

    //어떤 레이 아웃을 생성 할 것인가
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTodoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    //생성된 뷰에 무슨 데이터 를 넣을 것인가
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = listData[position]
        holder.setData(member, position)
    }

    //뷰 홀더
    //각 목록에 필요한 기능 들을 구현 하는 공간
    class Holder(val binding:ItemTodoListBinding):ViewHolder(binding.root){
        private val todoActivity = TodoActivity.getInstance()
        var mMember: DailyTodo? = null
        var mPosition:Int? = null

        init{
            binding.btnCheck.setOnClickListener {
                //Check 표시 변경 가능 할 수 있는 기능
                if(!(mMember!!.checkFlag)){
                    binding.btnCheck.setImageResource(R.drawable.iv_daily_todo_checked)
                    mMember!!.checkFlag = true
                }else{
                    binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
                    mMember!!.checkFlag = false
                }
            }

            binding.btnThreeDot.setOnClickListener {
                todoActivity?.showDailyBottomSheetDialog(mMember!!, mPosition!!)
            }
        }


        fun setData(member: DailyTodo, position:Int){
            this.mMember = member
            this.mPosition = position

            //text 설정
            binding.etTodo.setText(member.todo)

            //checkFlag = true, false
            if(mMember!!.checkFlag){
                binding.btnCheck.setImageResource(R.drawable.iv_daily_todo_checked)
            }
            else{
                binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
            }

            //to-do 수정 = true, false
            if(mMember!!.modifyTodoFlag){
                binding.etTodo.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#6B9AC4"))
                binding.etTodo.isEnabled = true
                binding.etTodo.setSelection(member.todo.length)
                binding.etTodo.requestFocus()


                binding.etTodo.setOnEditorActionListener { _, actionId, event ->
                    Log.d("Yuri", "키보드 접근")
                    Log.d("Yuri", "pressed key : $actionId")
                    if((actionId == EditorInfo.IME_ACTION_DONE)||(event.keyCode == KeyEvent.KEYCODE_ENTER)){
                        member.modifyTodoFlag = false
                        binding.etTodo.isEnabled = false
                        binding.etTodo.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00000000"))
                        member.todo = binding.etTodo.text.toString()
                        if(binding.etTodo.text.toString().isEmpty()) {
                            //삭제
                            todoActivity?.deleteTodo(member)
                        }
                        true
                    }else{
                        false
                    }
                }
            }
        }

        //TODO : to-do 이미지 추가, 삭제

    }
}