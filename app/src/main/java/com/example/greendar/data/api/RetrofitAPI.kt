package com.example.greendar.data.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitAPI {
    private const val BASE_URL = "http://35.216.10.67:8080"

    private var gson = GsonBuilder().setLenient().create()

    private val retrofit: Retrofit by lazy{
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val post: RegisterAPI by lazy{
        retrofit.create(RegisterAPI::class.java)
    }

    val getDaily:DailyTodoAPI by lazy{
        retrofit.create(DailyTodoAPI::class.java)
    }

    val getEvent:EventTodoAPI by lazy{
        retrofit.create(EventTodoAPI::class.java)
    }

}