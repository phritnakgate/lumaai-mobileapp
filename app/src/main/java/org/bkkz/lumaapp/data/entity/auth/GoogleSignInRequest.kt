package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class GoogleSignInRequest(
    @SerializedName("idToken")
    val idToken : String
)
