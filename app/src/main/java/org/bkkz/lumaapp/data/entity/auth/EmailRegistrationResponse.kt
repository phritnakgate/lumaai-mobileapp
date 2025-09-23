package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class EmailRegistrationResponse(
    @SerializedName("authorization_code")
    val authorizationCode : String? = null,
    @SerializedName("result")
    val result : String? = null,
    @SerializedName("error")
    val error : String? = null
)
