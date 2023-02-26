package com.example.calendar_new.model

data class Header(
    val code: Int,
    val message: String
)

data class Body(
    val date: String,
    val ratio : Float
)

//받아오는 형식
data class MyData(
    //@SerializedName("body") //SerializedName으로 하면 json 변수 값과 달라도 모델 만들 때 내가 원하는 변수 설정 가능
    val body:List<Body>,
    val header: Header
)
