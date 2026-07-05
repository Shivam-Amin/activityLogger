package com.activitylogger.data.repository

import com.activitylogger.data.local.dao.ActivityLogDao
import com.activitylogger.domain.mapper.ActivityLogMapper
import com.activitylogger.domain.model.ActivityLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLogRepository @Inject constructor(
    private val activityLogDao: ActivityLogDao
) {

    fun observeAllLogs(): Flow<List<ActivityLog>> {
        return activityLogDao.observeAllLogs().map { entities ->
            entities.map(ActivityLogMapper::toDomain)
        }
    }

    fun observeLogsBetween(startMillis: Long, endMillis: Long): Flow<List<ActivityLog>> {
        return activityLogDao.observeLogsBetween(startMillis, endMillis).map { entities ->
            entities.map(ActivityLogMapper::toDomain)
        }
    }

    suspend fun insertLog(log: ActivityLog): Long {
        return activityLogDao.insertLog(ActivityLogMapper.toEntity(log))
    }

    suspend fun deleteLogsByIds(logIds: List<Long>): Int {
        if (logIds.isEmpty()) {
            return 0
        }
        return activityLogDao.deleteLogsByIds(logIds)
    }

    suspend fun deleteLogsForDay(startOfDayMillis: Long, endOfDayMillis: Long): Int {
        return activityLogDao.deleteLogsBetween(startOfDayMillis, endOfDayMillis)
    }

    suspend fun deleteAllLogs(): Int {
        return activityLogDao.deleteAllLogs()
    }
}
