package com.example.greendar.data.api


import com.example.greendar.model.MyData
import retrofit2.Call
import retrofit2.http.*

interface PrivateMonthRatioInterface {
    @Headers("accept: application/json", "content-type: application/json")
    @GET("/api/v1/private/todo/monthly/ratio/{date}") //보내는 정보
    fun getData(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<MyData>
}

