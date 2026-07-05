package com.activitylogger.domain.model

enum class ActivityEventType(val displayName: String) {
    APP_OPENED("App opened"),
    APP_CLOSED("App closed"),
    HOME("Home screen"),
    OUTGOING_CALL("Outgoing call");

    companion object {
        fun fromRawValue(rawValue: String): ActivityEventType {
            return entries.firstOrNull { it.name == rawValue } ?: APP_OPENED
        }
    }
}
