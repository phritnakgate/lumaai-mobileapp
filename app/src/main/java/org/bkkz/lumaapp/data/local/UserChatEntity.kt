package org.bkkz.lumaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserChatEntity(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val flag : Int,
    val message : String? = null,
    val taskId : String? = null,
    val taskName : String? = null,
    val taskDesc : String? = null,
    val taskDateTime : String? = null,
    val taskCategory : Int? = null,
    val taskPriority : Int? = null,
    val isTaskActionCompleted : Boolean? = null,
    val searchUrl : String? = null,
    val generatedFormUrl : String? = null
)
