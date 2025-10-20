package org.bkkz.lumaapp.data.entity.user

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("displayName")
    val displayName: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("provider")
    val provider: Int,
    @SerializedName("googleRefreshToken")
    val googleRefreshToken: String?,
    @SerializedName("googleCalendarEmail")
    val googleCalendarEmail: String?,
)
