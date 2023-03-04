package com.example.greendar.data.api

import com.example.greendar.data.model.GetEventTodo
import com.example.greendar.data.model.PutEventTodoComplete
import com.example.greendar.data.model.ResponseDeleteEventImage
import com.example.greendar.data.model.ResponseEventTodoComplete
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface EventTodoAPI {

    @Headers("accept: application/json", "content-type: application/json")
    @GET("/api/v1/event/todo/{date}")
    fun getEventTodo(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<GetEventTodo>


    @Headers("accept: application/json", "content-type: application/json")
    @PUT("/api/v1/event/todo/complete")
    fun putEventTodoCheck(
        @Header("Authorization") token:String,
        @Body putEventTodoComplete: PutEventTodoComplete
    ):Call<ResponseEventTodoComplete>

    @Multipart
    @PUT("/api/v1/event/todo/image")
    fun putEventImage(
        @Header("Authorization") token:String,
        @Part file: MultipartBody.Part?,
        @Part ("eventTodoItemId") id:Int
    ):Call<ResponseEventTodoComplete>

    @DELETE("/api/v1/event/todo/image")
    fun deleteEventTodoImage(
        @Header("Authorization") token:String,
        @Query("eventTodoItemId") id:Int
    ):Call<ResponseDeleteEventImage>


}