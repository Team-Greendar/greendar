package com.example.greendar.data.model

data class PostRegisterUser(
    val email: String,
    val firebaseToken: String,
    val name: String,
    val password: String
)

data class ResponseRegisterUser(
    val header: Header,
)

data class Body(
    val email: String,
    val id: Int,
    val imageUrl: String,
    val message: String,
    val name: String,
    val password: String
)

data class Header(
    val message: String,
    val status: Int
)