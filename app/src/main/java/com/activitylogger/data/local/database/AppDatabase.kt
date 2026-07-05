package com.activitylogger.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.activitylogger.data.local.dao.ActivityLogDao
import com.activitylogger.data.local.entity.ActivityLogEntity

@Database(
    entities = [ActivityLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityLogDao(): ActivityLogDao
}
