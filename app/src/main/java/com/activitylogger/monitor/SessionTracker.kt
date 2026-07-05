package com.activitylogger.monitor

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionTracker @Inject constructor() {

    private val activeSessions = mutableMapOf<String, Long>()

    fun onAppMovedToForeground(packageName: String, timestampMillis: Long) {
        activeSessions[packageName] = timestampMillis
    }

    fun onAppMovedToBackground(packageName: String, timestampMillis: Long): Long? {
        val sessionStartMillis = activeSessions.remove(packageName) ?: return null
        val durationMillis = timestampMillis - sessionStartMillis
        if (durationMillis <= 0L) {
            return null
        }
        return durationMillis / 1_000L
    }

    fun clear() {
        activeSessions.clear()
    }
}
