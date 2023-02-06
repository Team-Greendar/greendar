package com.example.greendar.data.model

import com.google.gson.annotations.SerializedName

data class PostModel(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val password: String? = null
)