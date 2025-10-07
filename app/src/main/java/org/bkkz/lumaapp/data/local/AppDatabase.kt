package org.bkkz.lumaapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [UserChatEntity::class, UserReportEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userChatDao() : UserChatDao
    abstract fun userReportDao() : UserReportDao
}