package org.bkkz.lumaapp.data.entity.task

import com.google.gson.annotations.SerializedName

data class CreateTaskRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("dueTime")
    val dueTime: String,
    @SerializedName("category")
    val category: Int,
    @SerializedName("priority")
    val priority: Int
)
