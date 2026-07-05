package com.activitylogger.domain.model

data class ActivityLog(
    val id: Long = 0L,
    val timestampMillis: Long,
    val eventType: ActivityEventType,
    val appPackageName: String? = null,
    val appDisplayName: String? = null,
    val phoneNumber: String? = null,
    val contactName: String? = null,
    val durationSeconds: Long? = null
)
