package org.bkkz.lumaapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserReportDao {

    @Query("SELECT * FROM userreportentity ORDER BY downloadedTimeStamp DESC")
    fun getAllUserReport() : List<UserReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserReport(userReportEntity: UserReportEntity)

    @Query("DELETE FROM userreportentity")
    suspend fun deleteAllUserReport()
}