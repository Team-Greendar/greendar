package com.example.greendar.data.api

import com.example.greendar.data.model.GetEventTodo
import com.example.greendar.data.model.PutEventTodoComplete
import com.example.greendar.data.model.ResponseEventTodoComplete
import retrofit2.Call
import retrofit2.http.*

interface EventTodoAPI {

    @Headers("accept: application/json", "content-type: application/json")
    @GET("/api/v1/event/todo/{date}")
    fun getEventTodo(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<GetEventTodo>

    @PUT("/api/v1/event/todo/complete")
    fun putEventTodoCheck(
        @Header("Authorization") token:String,
        @Body putEventTodoComplete: PutEventTodoComplete
    ):Call<ResponseEventTodoComplete>


}