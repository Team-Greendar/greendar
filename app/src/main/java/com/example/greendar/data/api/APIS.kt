package com.example.greendar.data.api

import retrofit2.Call
import com.example.greendar.data.model.PostModel
import com.example.greendar.data.model.PostResult
import retrofit2.http.*

interface APIS {
    @Headers("accept: application/json", "content-type: application/json")
    @POST("api/hello")
    fun postUsers(
        @Body postUser:PostModel
    ):Call<PostResult>
}