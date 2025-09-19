package org.bkkz.lumaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserChat(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val flag : Int,
    val message : String?,
    val taskId : String?,
    val taskName : String?,
    val taskDesc : String?,
    val taskDateTime : String?,
    val isTaskActionCompleted : Boolean?,
    val searchUrl : String?
)
