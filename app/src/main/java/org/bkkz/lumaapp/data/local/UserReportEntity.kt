package org.bkkz.lumaapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserReportEntity(
    @PrimaryKey val fileNameKey : String,
    val localFilePath: String,
    val downloadedTimeStamp : Long
)
