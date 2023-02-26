package com.example.greendar.data.model

data class GetDailyTodo(
    val header: Head,
    val body: List<Body>
)

data class PostDailyNewTodo(
    val date: String,
    val task: String
)

data class PutDailyTodoChanged(
    val complete: String,
    val private_todo_id: String
)

data class PutDailyTodoTaskModify(
    val private_todo_id: String,
    val task:String
)

data class ResponseDailyNewTodo(
    val body: Body,
    val header: Header
)

data class Head(
    val code: Int,
    val message: String
)

data class Body(
    val complete: Boolean,
    val date: String,
    val imageUrl: String,
    val name: String,
    val private_todo_id: Int,
    val task: String
)