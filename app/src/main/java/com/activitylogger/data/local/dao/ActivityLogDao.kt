package com.activitylogger.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.activitylogger.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Query(
        """
        SELECT * FROM activity_logs
        WHERE timestampMillis BETWEEN :startMillis AND :endMillis
        ORDER BY timestampMillis DESC
        """
    )
    fun observeLogsBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs ORDER BY timestampMillis DESC")
    fun observeAllLogs(): Flow<List<ActivityLogEntity>>

    @Query("DELETE FROM activity_logs WHERE id IN (:logIds)")
    suspend fun deleteLogsByIds(logIds: List<Long>): Int

    @Query(
        """
        DELETE FROM activity_logs
        WHERE timestampMillis BETWEEN :startMillis AND :endMillis
        """
    )
    suspend fun deleteLogsBetween(startMillis: Long, endMillis: Long): Int

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllLogs(): Int
}
