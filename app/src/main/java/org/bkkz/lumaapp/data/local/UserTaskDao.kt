package org.bkkz.lumaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserTaskDao {
    @Query("SELECT * FROM usertaskentity WHERE id = :taskId LIMIT 1")
    suspend fun getUserTaskById(taskId: String): UserTaskEntity?

    @Query("SELECT * FROM usertaskentity WHERE dateTime LIKE :datePrefix || '%'")
    suspend fun getUserTasksByDatePrefix(datePrefix: String): List<UserTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserTask(userTaskEntity: UserTaskEntity)

    @Query("DELETE FROM usertaskentity WHERE id = :taskId")
    suspend fun deleteUserTaskById(taskId: String)

    @Query("UPDATE usertaskentity SET isFinished = :isFinished WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: String, isFinished: Boolean)

    @Update
    suspend fun updateTask(task: UserTaskEntity)

    @Query("DELETE FROM usertaskentity")
    suspend fun deleteAllUserTasks()

}