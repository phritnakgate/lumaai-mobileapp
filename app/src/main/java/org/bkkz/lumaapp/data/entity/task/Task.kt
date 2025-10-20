package org.bkkz.lumaapp.data.entity.task

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("isFinished")
    val isFinished: Boolean,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("category")
    val category: Int,
    @SerializedName("priority")
    val priority: Int,
    @SerializedName("isGoogleCalendarTask")
    val isGoogleCalendarTask: Boolean
) : Parcelable
