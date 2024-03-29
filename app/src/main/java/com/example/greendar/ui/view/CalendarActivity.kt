package com.example.greendar.ui.view

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html.fromHtml
import android.text.style.LineBackgroundSpan
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.greendar.R
import com.example.greendar.data.api.PrivateMonthRatioInterface
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.greendar.data.api.EventTodoRatioInterface
import com.example.greendar.data.recycler.EventTodo
import com.example.greendar.data.recycler.UserInfo
import com.example.greendar.data.recycler.UserInfo.token
import com.example.greendar.model.EventTodoRatio

import com.example.greendar.model.MyData
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.spans.DotSpan
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

const val BASE_URL = "http://35.216.10.67:8080"

class CalendarActivity : AppCompatActivity() {


    //[사진,텍스트 새로고침]
    lateinit var textView: TextView
    private lateinit var imageView:ImageView
    lateinit var imageButton: ImageButton
    private val changeImages: IntArray = intArrayOf(
        R.drawable.cheetah,
        R.drawable.giantotter,
        R.drawable.polarbear,
        R.drawable.staghorncoral,
        R.drawable.vendacycad,
        R.drawable.holly
    )
    private val imageTitle = mutableListOf("Cheetah","GiantOtter","PolarBear","Staghorn","Venda Cycad","Holly")
    private val imageG2 = mutableListOf(
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
        imageView = findViewById(R.id.holly_fix)
        imageButton = findViewById(R.id.image_change_btn)

        var index = 0

        imageButton.setOnClickListener{
            imageView.setImageResource(changeImages[index])
            titletextView.text = imageTitle[index]
            rangetextView.text = imageG2[index]
            index = (index +1) % changeImages.size
        }



        //[캘린더] 1. 변수선언
        val calendarView: MaterialCalendarView = findViewById(R.id.calendarView)
        val selectedDateTextView: TextView = findViewById(R.id.day_text)

        //[API] token, date 변수 선언
        var month: String

        //외부 사이크 링크 연결 변수선언
        val redlistclickbtn: ImageButton = findViewById(R.id.redlist_click_btn)


        //[캘린더] 2. 텍스트뷰에 불러오기(Date/일자별) : set up the DATE changed listener to update the selected month text view
        calendarView.setOnDateChangedListener { _, date, _ ->
            val formattedDate =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date.date)

            formattedDate.toString()

            //날짜 클릭 시 해당 TodoActivity 페이지로 전환 , formattedDate 데이터 전달
            val intent = Intent(this@CalendarActivity, TodoActivity::class.java)
            //intent.putExtra("selectedDate", formattedDate)
            //todo
            UserInfo.date = formattedDate
            Log.d("hhh", formattedDate.toString()) // 클릭 날짜 잘 넘어감 확인
            startActivity(intent)
        }


        //[캘린더] 2. 텍스트뷰에 불러오기(Month): Event(Eco), Private(Daily) 둘다
        calendarView.setOnMonthChangedListener { _, date ->
            val formattedMonth =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date?.date!!)

            //selectedMonthTextView.text = formattedMonth
            //selectedEventMonthTextView.text=formattedMonth
            selectedDateTextView.text = formattedMonth.substring(0, 7)
            month=formattedMonth.toString()

            getMyData(month) //API Daily
            getEventTodoRatio(month)//API Eco

        }

        //[링크] 캘린더 하단 ICUN 링크 연결
        redlistclickbtn.setOnClickListener {
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


    //[API] @Get Event(ECO)TodoRatioInterface 연결
    private fun getEventTodoRatio(month:String){
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(EventTodoRatioInterface::class.java)
        //Log.d("hhh", "Event(Eco) 연결 확인 중1")


        //ProfileSetting 183 Token Receive
        //todo
        val retrofitData = retrofitBuilder.getData(token = token , date = month)
        //Log.d("hhh", "Event(Eco) Token : $token")


        retrofitData.enqueue(object : Callback<EventTodoRatio?> {
            override fun onResponse(call: Call<EventTodoRatio?>, response: Response<EventTodoRatio?>) {
                //Log.d("hhh", "Event(Eco) 연결 확인 중2")
                //Log.d("hhh", "${response.code()}")


                if(response.isSuccessful){
                    //ratio만 리스트(bodyList 변수로 선언)로 받아오려고 추가한 변수들
                    val EventTodoRatio = response.body()
                    val bodyList = EventTodoRatio?.body?.ratio


                    val result: String = "<font color='#F4B942'><strong>Eco TO-DO</strong></font> COMPLETION RATE is <font color='#F4B942'><strong>${bodyList}</strong></font>%"

                    val selectedMonthTextView: TextView = findViewById(R.id.eventTODO_month_text)
                    selectedMonthTextView.text = fromHtml(result)
                    //Log.d("hhh", "Event(Eco) 연결 확인 중3")
                }else{
                    //Log.d("hhh", "연결 실패(Eco)")
                }
            }
            override fun onFailure(call: Call<EventTodoRatio?>, t: Throwable) {
                Toast.makeText(this@CalendarActivity, "서버 로직 에러", Toast.LENGTH_LONG).show()
                //Log.d("hhh", "서버 로직 에러(Eco)")
            }
        })


    }

    //[API] @Get Private(DAILY)MonthRatioInterface 연결
    private fun getMyData(month:String){
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(PrivateMonthRatioInterface::class.java)
        Log.d("hhh", "Private(DAILY) 연결 확인 중1")


        //ProfileSetting 183 Token Receive
        val retrofitData = retrofitBuilder.getData(token = token, date = month)
        Log.d("hhh", "Private(DAILY) Token : $token")

        retrofitData.enqueue(object : Callback<MyData?> {
            override fun onResponse(call: Call<MyData?>, response: Response<MyData?>) {
                Log.d("hhh", "Private(DAILY) 연결 확인 중2")
                Log.d("hhh", "${response.code()}")


                if(response.isSuccessful){
                    //ratio만 리스트(bodyList 변수로 선언)로 받아오려고 추가한 변수들
                    val myData = response.body()
                    val privateRatio = myData?.body?.ratio
                    //data 파싱

                    val result: String = "<font color='#6B9AC4'><strong>Daily TO-DO</strong></font> COMPLETION RATE is <font color='#6B9AC4'><strong>${privateRatio}</strong></font>%"
                    val selectedMonthTextView: TextView = findViewById(R.id.month_text)
                    selectedMonthTextView.text = fromHtml(result)
                    Log.d("hhh", "Private(DAILY) 연결 확인 중3")
                }else{
                    Log.d("hhh", "연결 실패(DAILY)")
                }
            }


            override fun onFailure(call: Call<MyData?>, t: Throwable) {
                Toast.makeText(this@CalendarActivity, "서버 로직 에러", Toast.LENGTH_LONG).show()
                Log.d("hhh", "서버 로직 에러(DAILY)")
            }
        })

    }
}



