package org.bkkz.lumaapp.data.entity.chat

import com.google.gson.annotations.SerializedName
import org.bkkz.lumaapp.data.entity.task.Task

data class LLMProcess(
    @SerializedName("intent")
    val intent : String,
    @SerializedName("message")
    val message : String? = null,
    @SerializedName("output")
    val output : List<Task>? = null
)
