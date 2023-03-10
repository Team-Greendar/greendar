package com.example.greendar.data.recycler

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.greendar.R
import com.example.greendar.data.api.RetrofitAPI
import com.example.greendar.data.model.PutEventTodoComplete
import com.example.greendar.data.model.ResponseEventTodoComplete
import com.example.greendar.data.recycler.UserInfo.default_Address
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
        private var mPosition: Int? = null

        init{
            binding.btnCheck.setOnClickListener {
                //Check 표시 변경 가능 할 수 있는 기능
                if (!(mMember!!.complete)) {
                    binding.btnCheck.setImageResource(R.drawable.iv_event_todo_checked)
                    val putEventTodo = PutEventTodoComplete(true, mMember!!.event_todo_id)
                    Log.e("Yuri", "이벤트 투두 id : ${mMember!!.event_todo_id}")
                    putEventModifyCheck(token, putEventTodo)
                } else {
                    binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
                    val putEventTodo = PutEventTodoComplete(false, mMember!!.event_todo_id)
                    putEventModifyCheck(token, putEventTodo)
                }
            }

            binding.ivPhoto.setOnClickListener {
                todoActivity?.showImageBottomSheetDialog(mMember!!.imageUrl, mMember!!.task, mMember!!.date)
            }

            binding.btnThreeDot.setOnClickListener {
                todoActivity?.showEventBottomSheetDialog(mMember!!, mPosition!!)
            }
        }

        fun setData(member:EventTodo, position: Int) {
            this.mMember = member
            this.mPosition = position

            //text 설정
            binding.etTodo.setText(member.task)

            //complete = true, false
            if (mMember!!.complete) {
                binding.btnCheck.setImageResource(R.drawable.iv_event_todo_checked)
            } else {
                binding.btnCheck.setImageResource(R.drawable.btn_todo_disabled)
            }

            //TODO : 이미지 초기 설정
            if (mMember!!.imageUrl == "EMPTY") {
                binding.ivPhoto.isEnabled = false
                binding.ivPhoto.setImageResource(R.drawable.iv_invisible_box)
            } else {
                binding.ivPhoto.isEnabled = true
                //var path = "https://images.unsplash.com/photo-1661956602868-6ae368943878?ixlib=rb-4.0.3&ixid=MnwxMjA3fDF8MHxlZGl0b3JpYWwtZmVlZHwxfHx8ZW58MHx8fHw%3D&auto=format&fit=crop&w=1200&q=60"
                //path = "https://storage.cloud.google.com/greendar_storage/test/Screenshot_20230222-182438_Greendar.jpg-q0l5Td.jpg"

                val path = default_Address + mMember!!.imageUrl

                //todo : http 이미지 를 못 불러 온다...
                if (member.imageUrl != "EMPTY") {
                    Log.d("Yuri", "link : ${member.imageUrl}")
                    Glide.with(binding.ivPhoto)
                        .load(path)
                        .listener(object : RequestListener<Drawable> {
                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
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
                        .transform(CenterCrop(), RoundedCorners(5))
                        .into(binding.ivPhoto)
                }
            }
        }

        /* ========= API 연결 함수 작성 ========= */
        //(api- 성공) to-do : check 여부 수정
        private fun putEventModifyCheck(token:String, putEventTodoComplete: PutEventTodoComplete){
            RetrofitAPI.getEvent.putEventTodoCheck(token, putEventTodoComplete)
                .enqueue(object:retrofit2.Callback<ResponseEventTodoComplete>{
                    override fun onResponse(
                        call: Call<ResponseEventTodoComplete>,
                        response: Response<ResponseEventTodoComplete>
                    ) {
                        if (response.code() == 200) {
                            Log.e("Yuri", "이벤트 투두 체크 : 서버 연결 성공")
                            mMember?.complete = response.body()!!.body.complete
                            Log.e("Yuri", "task : ${response.body()!!.body.task}")
                            Log.e("Yuri", "complete : ${mMember?.complete}")
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

    }
}