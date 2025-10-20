package org.bkkz.lumaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserTaskEntity(
    @PrimaryKey val id : String,
    val name: String,
    val description: String? = null,
    val dateTime: String,
    val isFinished: Boolean,
    val userId: String,
    val category: Int,
    val priority: Int,
    val isGoogleCalendarTask: Boolean
)
