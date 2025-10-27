package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("email")
    val email : String
)
