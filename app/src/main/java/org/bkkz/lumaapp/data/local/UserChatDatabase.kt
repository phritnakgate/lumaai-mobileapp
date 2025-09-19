package org.bkkz.lumaapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserChat::class], version = 1)
abstract class UserChatDatabase : RoomDatabase() {
    abstract fun userChatDao() : UserChatDao
}