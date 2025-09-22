package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class EmailRegistrationResponse(
    @SerializedName("authorization_code")
    val authorizationCode : String,
    @SerializedName("result")
    val result : String
)
