package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class LogoutRequest(
    @SerializedName("refresh_token")
    val refreshToken : String
)
