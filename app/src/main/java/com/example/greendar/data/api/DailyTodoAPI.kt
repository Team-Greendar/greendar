package com.example.greendar.data.api

import com.example.greendar.data.model.GetDailyTodo
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface DailyTodoAPI {

    @Headers("accept: application/json", "content-type: application/json")
    @GET("api/v1/private/todo/{date}")
    fun getDailyTodo(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<GetDailyTodo>


}