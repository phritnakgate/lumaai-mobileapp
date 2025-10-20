package org.bkkz.lumaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserChatDao {

    @Query("SELECT * FROM userchatentity")
    fun getAllUserChat() : List<UserChatEntity>

    @Insert
    suspend fun insertUserChat(userChatEntity: UserChatEntity)

    @Query("DELETE FROM userchatentity")
    suspend fun deleteAllUserChat()

    @Query("UPDATE userchatentity SET isTaskActionCompleted = 1")
    suspend fun confirmActionAll()

    @Query("UPDATE userchatentity SET isTaskActionCompleted = 1 WHERE id = :dbId")
    suspend fun confirmAction(dbId: Int)

    @Query("DELETE FROM userchatentity WHERE flag=-1")
    suspend fun deleteThinkingChat()

    @Query("SELECT * FROM userchatentity WHERE flag=-1")
    suspend fun checkThinkingChat() : List<UserChatEntity>
}