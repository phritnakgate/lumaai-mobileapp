package org.bkkz.lumaapp.data.entity.google_calendar

import com.google.gson.annotations.SerializedName

data class CalendarEventRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("startTime")
    val startTime: String,
    @SerializedName("endTime")
    val endTime: String,
    @SerializedName("ownerEmail")
    val ownerEmail: String,
    @SerializedName("appTaskTime")
    val appTaskTime: String? = null
)
