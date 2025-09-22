package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class EmailSignInResponse(
    @SerializedName("code")
    val code : String
)
