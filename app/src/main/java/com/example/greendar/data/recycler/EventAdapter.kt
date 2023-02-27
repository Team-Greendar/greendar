package com.example.greendar.data.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.greendar.R
import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.PutEventTodoComplete
import com.example.greendar.data.model.ResponseEventTodoComplete
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.databinding.ItemTodoListBinding
import com.example.greendar.ui.view.TodoActivity
import retrofit2.Call
import retrofit2.Response

class EventAdapter:RecyclerView.Adapter<EventAdapter.Holder>() {

    var listData = mutableListOf<EventTodo>()

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTodoListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val member = listData[position]
        holder.setData(member, position)
    }

    class Holder(val binding: ItemTodoListBinding):ViewHolder(binding.root){
        private val todoActivity = TodoActivity.getInstance()
        var mMember: EventTodo? = null
        var mPosition: Int? = null

        init{
            binding.btnCheck.setOnClickListener {
                //todo : Check 표시 변경 가능 할 수 있는 기능
                if (!(mMember!!.complete)) {
                    binding.btnCheck.setImageResource(R.drawable.iv_daily_todo_checked)
                    val putEventTodo = PutEventTodoComplete(true, mMember!!.event_todo_id.toString())
                    putEventModifyCheck(token, putEventTodo)
                    mMember!!.complete = true
                } else {
                    binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
                    val putEventTodo = PutEventTodoComplete(false, mMember!!.event_todo_id.toString())
                    putEventModifyCheck(token, putEventTodo)
                    mMember!!.complete = false
                }
            }

            binding.btnThreeDot.setOnClickListener {
                todoActivity?.showEventBottomSheetDialog(mMember!!, mPosition!!)
            }
        }

        fun setData(member:EventTodo, position: Int){
            this.mMember = member
            this.mPosition = position

            //text 설정
            binding.etTodo.setText(member.task)

            //complete = true, false
            if (mMember!!.complete) {
                binding.btnCheck.setImageResource(R.drawable.iv_daily_todo_checked)
            } else {
                binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
            }
        }

        //TODO : to-do 이미지 추가, 삭제

        /* ========= API 연결 함수 작성 ========= */
        //todo (api) : check 여부 수정
        private fun putEventModifyCheck(token:String, putEventTodoComplete: PutEventTodoComplete){
            RetrofitAPI.getEvent.putEventTodoCheck(token, putEventTodoComplete)
                .enqueue(object:retrofit2.Callback<ResponseEventTodoComplete>{
                    override fun onResponse(
                        call: Call<ResponseEventTodoComplete>,
                        response: Response<ResponseEventTodoComplete>
                    ) {
                        if (response.code() == 200) {
                            Log.e("Yuri", "이벤트 투두 체크 : 서버 연결 성공")
                            //TODO : response 바디 수정 해야 할 듯... 싶은데.. 수정하고 complete 갱신
                        } else {
                            Log.e("Yuri", "이벤트 투두 체크 : sth wrong..! OMG")
                        }
                    }
                    override fun onFailure(call: Call<ResponseEventTodoComplete>, t: Throwable) {
                        Log.e("Yuri", "이벤트 투두 체크 여부 서버 연결 실패")
                        Log.e("Yuri", t.toString())
                    }
                })
        }

        //todo (api) 이미지
    }
}