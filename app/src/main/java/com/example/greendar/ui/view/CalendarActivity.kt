package com.example.calendar_new

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.calendar_new.model.Body
import com.example.calendar_new.model.MyData
import com.google.gson.Gson
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Header
import java.text.SimpleDateFormat
import java.util.*

const val BASE_URL = "http://35.216.10.67:8080"

class CalendarActivity : AppCompatActivity() {

    //[사진 변환]
    lateinit var imageView:ImageView
    lateinit var imageButton: ImageButton
    val changeImages:IntArray = intArrayOf(
        R.drawable.cheetah,
        R.drawable.giantotter,
        R.drawable.polarbear,
        R.drawable.staghorncoral,
        R.drawable.vendacycad,
        R.drawable.holly
    )


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        //[사진 변환]
        imageView = findViewById(R.id.holly_fix);
        imageButton = findViewById(R.id.image_change_btn)

        imageButton.setOnClickListener {
            val random = kotlin.random.Random
            imageView.setImageResource(changeImages[random.nextInt(changeImages.size)])
        }


        //[캘린더] 1. 변수선언
        val calendarView: MaterialCalendarView = findViewById(R.id.calendarview)
        val selectedDateTextView: TextView = findViewById(R.id.day_text)
        val selectedMonthTextView: TextView = findViewById(R.id.month_text)
        var month:String ="1"
        //외부 사이크 링크 연결 변수선언
        val redlist_click_btn: ImageButton = findViewById(R.id.redlist_click_btn)

        /*Run Affect X
        //initialize the selected month text view to display the current month
        */
        /**
         * SimpleDateFormat : Date -> String
         *//*
        val currentDate = CalendarDay.today()
        val currentMonth = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentDate.date)
        selectedMonthTextView.text = currentMonth*/


        //[캘린더] 2. 텍스트뷰에 불러오기(Date/일자별) : set up the DATE changed listener to update the selected month text view
        calendarView.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date?.date)
                selectedDateTextView.text = formattedDate
            }
        })

        //[캘린더] 2. 텍스트뷰에 불러오기(Month): set up the MONTH changed listener to update the selected month text view
        calendarView.setOnMonthChangedListener { widget, date ->
            val formattedMonth =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date?.date)
            selectedMonthTextView.text = formattedMonth
            month=formattedMonth.toString()
            getMyData(month)

        }


        //[링크] 캘린더 하단 ICUN 링크 연결
        redlist_click_btn.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://www.iucnredlist.org/")
            startActivity(openURL)
        }
    }



    //[옵션메뉴] 캘린더 우측 상단 옵션 메뉴 설정
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }
    //[옵션메뉴] 클릭 시 해당 페이지로 이동
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> {
                val item1 = Intent(this, FindfriendsActivity::class.java)
                startActivity(item1)
                return true
            }
            R.id.item2 -> {
                val item2 = Intent(this, SettingsActivity::class.java)
                startActivity(item2)
                return true
            }
            else -> return false
        }

    }




    //[API] @Get PrivateMonthRatioInterface 연결
    private fun getMyData(month:String){
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PrivateMonthRatioInterface::class.java)
        Log.d("hhh", "연결 확인 중2")

       /* val retrofitData = retrofitBuilder.getData(token = "T4kZ7TfIOUXRVYAa3GXOWnJllEr1", date = "2021-12-23")*/
        val retrofitData = retrofitBuilder.getData(token = "222222", date = month)


        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                Log.d("hhh", "연결 확인 중3")
                Log.d("hhh", "${response.code()}")

                if(response.isSuccessful){
                    val result: MyData? = response.body()
                    val selectedMonthTextView: TextView = findViewById(R.id.month_text)
                    selectedMonthTextView.text = result.toString()
                    Log.d("hhh", "연결 확인 중4")

                }else{
                    Log.d("hhh", "연결 실패")
                }

            }


            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Toast.makeText(this@CalendarActivity, "서버 로직 에러", Toast.LENGTH_LONG).show()
                Log.d("hhh", "서버 로직 에러")
            }
        })


    }

}



