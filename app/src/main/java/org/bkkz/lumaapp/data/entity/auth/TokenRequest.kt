package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("grantType")
    val grantType : String,
    @SerializedName("code")
    val code : String? = null,
    @SerializedName("codeVerifier")
    val codeVerifier : String? = null,
    @SerializedName("refreshToken")
    val refreshToken : String? = null
)
