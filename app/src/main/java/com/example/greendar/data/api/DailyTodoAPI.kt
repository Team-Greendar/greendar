package com.example.greendar.data.api

import com.example.greendar.data.model.*
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DailyTodoAPI {

    @Headers("accept: application/json", "content-type: application/json")
    @GET("api/v1/private/todo/{date}")
    fun getDailyTodo(
        @Header("Authorization") token:String,
        @Path("date") date:String
    ): Call<GetDailyTodo>

    @POST("/api/v1/private/todo")
    fun postDailyNewTodo(
        @Header("Authorization") token:String,
        @Body postDailyNewTodo: PostDailyNewTodo
    ):Call<ResponseDailyNewTodo>

    @PUT("/api/v1/private/todo/complete")
    fun putDailyTodoCheck(
        @Header("Authorization") token:String,
        @Body putDailyTodoChanged: PutDailyTodoChanged
    ):Call<ResponseDailyNewTodo>

    @PUT("/api/v1/private/todo/task")
    fun putDailyTodoTaskModify(
        @Header("Authorization") token:String,
        @Body putDailyTodoTaskModify: PutDailyTodoTaskModify
    ):Call<ResponseDailyNewTodo>

    @DELETE("/api/v1/private/todo/{private_to_do}")
    fun deleteDailyTodo(
        @Header("Authorization") token:String,
        @Path("private_to_do") private_todo_id:Int
    ):Call<ResponseDeleteDailyTodo>

}