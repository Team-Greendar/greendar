package com.example.greendar.data.model

data class PostRegisterUser(
    val email: String,
    val firebaseToken: String,
    val name: String,
    val password: String,
    val imageUrl: String,
    val message:String
)

data class ResponseRegisterUser(
    val header: Header,
)

data class ResponseProfileImage(
    val body: String,
    val header: Header
)

data class Header(
    val message: String,
    val status: Int
)

