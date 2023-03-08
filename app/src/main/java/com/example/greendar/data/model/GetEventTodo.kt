package com.example.greendar.data.model

data class GetEventTodo(
    val header: Header,
    val body: List<EventBody>
)

data class EventBody(
    val complete: Boolean,
    val date: String,
    val eventTodoItemId: Int,
    val imageUrl: String,
    val task: String
)

data class PutEventTodoComplete(
    val complete: Boolean,
    val eventTodoItemId: Int
)

data class ResponseEventTodoComplete(
    val header: Header,
    val body: EventBody
)

data class ResponseDeleteEventImage(
    val header:Header,
    val body:Boolean
)
