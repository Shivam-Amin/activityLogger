package com.activitylogger.domain.mapper

import com.activitylogger.data.local.entity.ActivityLogEntity
import com.activitylogger.domain.model.ActivityEventType
import com.activitylogger.domain.model.ActivityLog

object ActivityLogMapper {

    fun toDomain(entity: ActivityLogEntity): ActivityLog {
        return ActivityLog(
            id = entity.id,
            timestampMillis = entity.timestampMillis,
            eventType = ActivityEventType.fromRawValue(entity.eventType),
            appPackageName = entity.appPackageName,
            appDisplayName = entity.appDisplayName,
            phoneNumber = entity.phoneNumber,
            contactName = entity.contactName,
            durationSeconds = entity.durationSeconds
        )
    }

    fun toEntity(domain: ActivityLog): ActivityLogEntity {
        return ActivityLogEntity(
            id = domain.id,
            timestampMillis = domain.timestampMillis,
            eventType = domain.eventType.name,
            appPackageName = domain.appPackageName,
            appDisplayName = domain.appDisplayName,
            phoneNumber = domain.phoneNumber,
            contactName = domain.contactName,
            durationSeconds = domain.durationSeconds
        )
    }
}
