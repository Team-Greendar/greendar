package com.example.greendar.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.calendar_new.FindfriendsActivity
import com.example.calendar_new.PrivateMonthRatioInterface
import com.example.calendar_new.SettingsActivity
import com.example.calendar_new.model.MyData
import com.prolificinteractive.materialcalendarview.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import com.example.greendar.R

const val BASE_URL = "http://35.216.10.67:8080"

class CalendarActivity : AppCompatActivity() {


    //[사진,텍스트 새로고침]
    lateinit var textView: TextView
    lateinit var imageView:ImageView
    lateinit var imageButton: ImageButton
    val changeImages: IntArray = intArrayOf(
        R.drawable.cheetah,
        R.drawable.giantotter,
        R.drawable.polarbear,
        R.drawable.staghorncoral,
        R.drawable.vendacycad,
        R.drawable.holly
    )
    val ImageTitle = mutableListOf("Cheetah","GiantOtter","PolarBear","Staghorn","Venda Cycad","Holly")
    val ImageG2 = mutableListOf(
        "Algeria, Angola(Savana)",
        "Amazon(Forest, Marine Neritic)",
        "Canada, Greenland, Norway, Alaska",
        "Anguilla(Marine Neritic)",
        "SouthAfrica(Savana)",
        "Albania(Forest, Shrub land)"
        )


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        //[사진,텍스트 새로고침]
        val titletextView = findViewById<TextView>(R.id.holly_title)
        val rangetextView = findViewById<TextView>(R.id.holly_geographic_range_2)
        imageView = findViewById(R.id.holly_fix);
        imageButton = findViewById(R.id.image_change_btn)

        var index = 0

        imageButton.setOnClickListener{
            imageView.setImageResource(changeImages[index])
            titletextView.text = ImageTitle[index]
            rangetextView.text = ImageG2[index]
            index = (index +1) % changeImages.size
        }


        //[캘린더] 1. 변수선언
        val calendarView: MaterialCalendarView = findViewById(R.id.calendarview)
        val selectedDateTextView: TextView = findViewById(R.id.day_text)
        val selectedMonthTextView: TextView = findViewById(R.id.month_text)

        //[API] token, date 변수 선언
        var month:String ="1"
        var date: String = "1"

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
        //[Bar 아이콘 넣기] 1. DayViewDecorator 인터페이스 사용해서 선택된 날짜에 아이콘 추가하기
        //[그냥 아이콘만 찍힘]
            class SpecialDayDecorator(context: Context) : DayViewDecorator {
            private val drawable = ContextCompat.getDrawable(context, R.drawable.ic_leaf)!!
            private var specialDate: CalendarDay? = null

            fun setSpecialDate(date:CalendarDay){
                specialDate = date
            }
            //[Bar 아이콘 넣기] shouldDecorate() : 날짜에 아이콘 넣기 ★조건★ 결정 : True/False 반환
            override fun shouldDecorate(day: CalendarDay?): Boolean {
                return day == specialDate
                return day == specialDate
            }
            override fun decorate(view: DayViewFacade?) {
                view?.setSelectionDrawable(drawable)
            }
        }
        val selectedDateDecorator = SpecialDayDecorator(this)
        calendarView.addDecorator(selectedDateDecorator)


        /*//[Bar 아이콘 넣기] 2. OnDateChangedListner 이용해서 선택된 날짜에 아이콘 넣기
        calendarView.setOnDateChangedListener(object : OnDateSelectedListener {
            override fun onDateSelected(
                widget: MaterialCalendarView,
                date: CalendarDay,
                selected: Boolean
            ) {
                val formattedDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date?.date)
                selectedDateTextView.text = formattedDate

                selectedDateDecorator.setSpecialDate(date)
                // 선택된 날짜의 셀에 Span을 추가
                calendarView.invalidateDecorators()
            }
        })*/


        //[원래 했던 것 : 그냥 날짜 클릭 시 하단 텍스트 뷰에 해당 날짜 띄우는 형태]
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
                val item2 = Intent(this, ShowAccountAddfriendsActivity::class.java)
                startActivity(item2)
                return true
            }
            R.id.item3 -> {
                val item3 = Intent(this, SettingsActivity::class.java)
                startActivity(item3)
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


        /*토큰으로 사용자 구별해서 완료 비율을 띄우는겨*/
        val retrofitData = retrofitBuilder.getData(token = "222222" , date = month)


        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                Log.d("hhh", "연결 확인 중3")
                Log.d("hhh", "${response.code()}")


                if(response.isSuccessful){
                    //ratio만 리스트(bodyList 변수로 선언)로 받아오려고 추가한 변수들
                    val myData = response.body()
                    val bodyList = myData?.body
                    val ratioList = bodyList?.map { it.ratio }
                    val ratioSum = ratioList?.sum()

                   /* //ratioSum을 Bar 형태로 나타내기
                    val progressBar : ProgressBar = findViewById(R.id.progress_bar)
                    progressBar.progress = (ratioSum?: 0) as Int
                    progressBar.max = 100*/

                    /*val result : String = "PRIVATE TO-DO COMPLETION RATE is ${ratioSum ?: 0}%"*/
                    val result : String = "PRIVATE TO-DO COMPLETION RATE is ${ratioSum}%"
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




