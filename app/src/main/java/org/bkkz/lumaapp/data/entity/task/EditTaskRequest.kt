package org.bkkz.lumaapp.data.entity.task

import com.google.gson.annotations.SerializedName

data class EditTaskRequest(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("dateTime")
    val dateTime: String? = null,
    @SerializedName("isFinished")
    val isFinished: Boolean? = null
)
