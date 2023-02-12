package com.example.calendar_0209

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarActivity : AppCompatActivity() {
    private lateinit var textMonth : TextView
    private lateinit var calendarAdapter:CalendarAdapter
    private lateinit var calendar : Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        //현재 기준 날짜 가져오기 Logcat
        val calendar = Calendar.getInstance()
        //Log.d("hhh",SimpleDateFormat("yyyy-MM-dd").format(calender.time))
        calendarAdapter = CalendarAdapter(this)
        val recyclerView : RecyclerView = findViewById(R.id.recycler_view)
        textMonth = findViewById(R.id.text_month)
        recyclerView.layoutManager = GridLayoutManager(this,7 ) //월~금 표현 //span count : 가로로 몇개를 나타낼 건가 일주일 7개
        recyclerView.adapter = calendarAdapter

        val btnLeft : ImageButton = findViewById<ImageButton>(R.id.calendar_left_btn)
        val btnRight : ImageButton = findViewById<ImageButton>(R.id.calendar_right_btn)
        btnLeft.setOnClickListener{
            var month = calendar.get(Calendar.MONTH)-1
            var year = calendar.get(Calendar.YEAR)
            if(month == -1) {
                month = 11
                year = year - 1
            }
            calendar.set(year, month, calendar.get(Calendar.DAY_OF_MONTH))
//            val lastMonth = Calendar.getInstance()
//            lastMonth.set(calendar.get(Calendar.YEAR),month, calendar.get(Calendar.DAY_OF_MONTH))
//            textMonth.setText(SimpleDateFormat("yyyy.MM").format(lastMonth.time))
            calendarShow(calendar)
        }
        btnRight.setOnClickListener{
            var month = calendar.get(Calendar.MONTH)+1
            var year = calendar.get(Calendar.YEAR)
            if (month == 12) {
                month = 0
                year=year+1
            }
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.YEAR, year)
            calendarShow(calendar)
        }

       calendarShow(calendar)

    }

    private fun calendarShow(calendar:Calendar) {
        val newCalendar = Calendar.getInstance()
        newCalendar.timeInMillis = calendar.timeInMillis
        textMonth.setText(SimpleDateFormat("yyyy.MM").format(newCalendar.time))
        val firstDay = newCalendar.getActualMinimum(Calendar.DAY_OF_MONTH)
        val lastDay = newCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        //Log.d("hhh", firstDay.toString() + "/" + lastDay.toString())
        val arrayDay = ArrayList<Long>()
        //calendar.set(Calendar.DAY_OF_MONTH, firstDay)


        for(i:Int in firstDay .. lastDay){
            newCalendar.set(Calendar.DAY_OF_MONTH, i)

            val dayOfWeek = newCalendar.get(Calendar.DAY_OF_WEEK)
            if(i==1 && dayOfWeek > 1){
                for(j:Int in 1..dayOfWeek -1) {
                    val lastCalendar = Calendar.getInstance()
                    var month = newCalendar.get(Calendar.MONTH)-1
                    var year = newCalendar.get(Calendar.YEAR)
                    if(month == -1) {
                        month = 11
                        year = year - 1
                    }
                    lastCalendar.set(Calendar.YEAR, month)
                    lastCalendar.set(Calendar.MONTH, month)
                    val lastMonth_lastDay =
                        (lastCalendar.getActualMaximum(Calendar.DAY_OF_MONTH) - (j - 1))
                    lastCalendar.set(Calendar.DAY_OF_MONTH, lastMonth_lastDay)
                    arrayDay.add(lastCalendar.timeInMillis)
                    Collections.sort(arrayDay) //날짜 순서대로 정렬
                }
            }
            arrayDay.add(newCalendar.timeInMillis) // 시간/년/월 싹다 숫자로 변형
            //Log.d("hhh", dayOfWeek.toString()) // 숫자로 요일 가져오기
            //Log.d("hhh", i.toString())
        }
        calendarAdapter.setList(arrayDay, newCalendar.get(Calendar.MONTH))
        //Log.d("hhh",firstDay.toString() + "/" + lastDay.toString()) //1~31일 날짜 가져오기
    }
}