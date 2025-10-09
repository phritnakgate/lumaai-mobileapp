package org.bkkz.lumaapp.data.entity.google_calendar

import com.google.gson.annotations.SerializedName

data class GoogleAuthRequest(
    @SerializedName("authCode")
    val authCode : String
)
