package com.example.greendar.model

data class EventHeader(
    val status: Int,
    val message: String
)

data class EventBody(
    val date: String,
    val ratio : Float
)

//받아오는 형식
data class EventTodoRatio(
    //@SerializedName("body") //SerializedName으로 하면 json 변수 값과 달라도 모델 만들 때 내가 원하는 변수 설정 가능
    val body:List<EventBody>,
    val header: EventHeader
)