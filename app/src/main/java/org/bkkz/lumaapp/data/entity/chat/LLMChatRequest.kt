package org.bkkz.lumaapp.data.entity.chat

import com.google.gson.annotations.SerializedName

data class LLMChatRequest(
    @SerializedName("text")
    val text: String
)
