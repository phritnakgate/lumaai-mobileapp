package org.bkkz.lumaapp.data.entity.task

import com.google.gson.annotations.SerializedName

data class Task(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("isFinished")
    val isFinished: Boolean,
    @SerializedName("userId")
    val userId: String
)
