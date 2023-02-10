package com.example.calendar_0209

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.view.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.channels.ReceiveChannel
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarAdapter(val context:Context) : RecyclerView.Adapter<CalendarAdapter.ItemView>() {
    private val array = ArrayList<Long>()
    private var month  = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemView {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day, parent, false)
        return ItemView(view)
    }

    override fun onBindViewHolder(holder:CalendarAdapter.ItemView, position:Int) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = array.get(position)
        val month = calendar.get(Calendar.MONTH)
        if(this.month != month){
            holder.background.setBackgroundColor(Color.parseColor("#44cccccc"))
        }else{
            holder.background.setBackgroundColor(Color.WHITE)
        }
        holder.textDay.setText(SimpleDateFormat("dd").format(calendar.time))

        //날짜 클릭
        holder.itemView.setOnClickListener{
            Toast.makeText(
                context,
                SimpleDateFormat("yyyy-MM-dd").format(calendar.time), Toast.LENGTH_LONG).show()
        }

    }

    override fun getItemCount(): Int {
        return array.size
    }
    fun setList(array:ArrayList<Long>, month:Int){
        this.month = month
        this.array.clear()
        this.array.addAll(array)
        notifyDataSetChanged()
    }
    class ItemView(view: View) : RecyclerView.ViewHolder(view){
        val textDay : TextView = view.findViewById(R.id.text_day)
        val background : ConstraintLayout = view.findViewById(R.id.background)
    }

}