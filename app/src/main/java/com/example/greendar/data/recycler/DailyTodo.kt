package com.example.greendar.data.recycler

data class DailyTodo(
    var complete:Boolean,
    var date:String,
    var imageUrl:String,
    var name:String,
    var private_todo_id:Int,
    var task:String,
    var modifyClicked:Boolean
)