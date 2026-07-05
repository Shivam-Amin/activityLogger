package com.activitylogger.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "activity_logs",
    indices = [
        Index(value = ["timestampMillis"]),
        Index(value = ["eventType", "timestampMillis"])
    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampMillis: Long,
    val eventType: String,
    val appPackageName: String? = null,
    val appDisplayName: String? = null,
    val phoneNumber: String? = null,
    val contactName: String? = null,
    val durationSeconds: Long? = null
)
