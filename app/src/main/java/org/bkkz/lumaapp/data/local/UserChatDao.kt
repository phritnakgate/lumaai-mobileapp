package org.bkkz.lumaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserChatDao {

    @Query("SELECT * FROM userchat")
    fun getAllUserChat() : List<UserChat>

    @Insert
    suspend fun insertUserChat(userChat: UserChat)

    @Query("DELETE FROM userchat")
    suspend fun deleteAllUserChat()
}