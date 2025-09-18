package org.bkkz.lumaapp.data.entity.chat_history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChatHistory (
    @SerializedName("intent")
    val intent : List<String>,
    @SerializedName("userId")
    val userId : String,
    @SerializedName("userText")
    val userText : String,
    @SerializedName("modelResponse")
    val modelResponse : String,
    @SerializedName("timeStamp")
    val timeStamp : String
): Parcelable