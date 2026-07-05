package com.activitylogger.ui.logs

import com.activitylogger.domain.model.ActivityLog
import com.activitylogger.util.DateTimeUtils

data class LogItemUiModel(
    val id: Long,
    val title: String,
    val subtitle: String,
    val timestampLabel: String,
    val durationLabel: String?
)

object LogItemUiMapper {

    fun toUiModel(log: ActivityLog): LogItemUiModel {
        val title = buildTitle(log)
        val subtitle = buildSubtitle(log)
        val durationLabel = log.durationSeconds?.let(DateTimeUtils::formatDuration)

        return LogItemUiModel(
            id = log.id,
            title = title,
            subtitle = subtitle,
            timestampLabel = DateTimeUtils.formatDateTime(log.timestampMillis),
            durationLabel = durationLabel
        )
    }

    private fun buildTitle(log: ActivityLog): String {
        return when (log.eventType) {
            com.activitylogger.domain.model.ActivityEventType.APP_OPENED ->
                "Opened ${log.appDisplayName ?: "app"}"
            com.activitylogger.domain.model.ActivityEventType.APP_CLOSED ->
                "Closed ${log.appDisplayName ?: "app"}"
            com.activitylogger.domain.model.ActivityEventType.HOME ->
                "Home screen"
            com.activitylogger.domain.model.ActivityEventType.OUTGOING_CALL ->
                "Outgoing call"
        }
    }

    private fun buildSubtitle(log: ActivityLog): String {
        return when (log.eventType) {
            com.activitylogger.domain.model.ActivityEventType.OUTGOING_CALL -> {
                val contact = log.contactName ?: log.phoneNumber ?: "Unknown number"
                contact
            }
            else -> log.appPackageName ?: log.eventType.displayName
        }
    }
}
