package com.example.greendar.data.model

data class GetDailyTodo(
    val header: Head,
    val body: List<Body>

)

data class Body(
    val complete: Boolean,
    val date: String,
    val imageUrl: String,
    val memberName: String,
    val private_todo_id: Int,
    val task: String
)

data class Head(
    val code: Int,
    val message: String
)