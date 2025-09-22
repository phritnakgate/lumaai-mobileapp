package org.bkkz.lumaapp.data.entity.auth

import com.google.gson.annotations.SerializedName

data class EmailRegistrationRequest (
    @SerializedName("email")
    val email : String,
    @SerializedName("password")
    val password : String,
    @SerializedName("name")
    val name : String,
    @SerializedName("codeChallenge")
    val codeChallenge : String,
    @SerializedName("codeChallengeMethod")
    val codeChallengeMethod : String = "S256",
)