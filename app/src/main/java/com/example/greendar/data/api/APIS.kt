package com.example.greendar.data.api

import com.example.greendar.data.model.PostRegisterUser
import com.example.greendar.data.model.ResponseRegisterUser
import retrofit2.Call
import retrofit2.http.*

interface APIS {
    @Headers("accept: application/json", "content-type: application/json")
    @POST("/api/v1/member")
    fun postRegisterUser(
        @Body postRegisterUser: PostRegisterUser
    ):Call<ResponseRegisterUser>

    @POST("/api/v1/member/validity")
    fun postFindUser(
        @Header("Authorization") token:String
    ):Call<ResponseRegisterUser>
}