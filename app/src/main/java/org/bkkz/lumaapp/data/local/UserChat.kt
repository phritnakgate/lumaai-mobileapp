package org.bkkz.lumaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserChat(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val flag : Int,
    val message : String? = null,
    val taskId : String? = null,
    val taskName : String? = null,
    val taskDesc : String? = null,
    val taskDateTime : String? = null,
    val isTaskActionCompleted : Boolean? = null,
    val searchUrl : String? = null
)
