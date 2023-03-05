package com.example.greendar.data.api

import com.example.calendar_new.model.EventTodoRatio
import com.example.calendar_new.model.MyData
import retrofit2.Call
import retrofit2.http.*

interface EventTodoRatioInterface {
    @Headers("accept: application/json", "content-type: application/json")
    @GET("/api/v1/event/todo/monthly/ratio/{date}")
    fun getData(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<EventTodoRatio>
}

