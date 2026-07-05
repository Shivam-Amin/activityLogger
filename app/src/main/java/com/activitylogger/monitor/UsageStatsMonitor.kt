package com.activitylogger.monitor

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.activitylogger.data.repository.ActivityLogRepository
import com.activitylogger.domain.model.ActivityEventType
import com.activitylogger.domain.model.ActivityLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageStatsMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityLogRepository: ActivityLogRepository,
    private val sessionTracker: SessionTracker,
    private val launcherDetector: LauncherDetector,
    private val appLabelResolver: AppLabelResolver
) {

    private val usageStatsManager: UsageStatsManager =
        context.getSystemService(UsageStatsManager::class.java)

    private var lastQueryTimeMillis: Long = System.currentTimeMillis() - POLL_LOOKBACK_MILLIS
    private var lastForegroundPackage: String? = null

    suspend fun pollUsageEvents() {
        val currentTimeMillis = System.currentTimeMillis()
        val usageEvents = usageStatsManager.queryEvents(lastQueryTimeMillis, currentTimeMillis)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            handleUsageEvent(event)
        }

        lastQueryTimeMillis = currentTimeMillis
    }

    private suspend fun handleUsageEvent(event: UsageEvents.Event) {
        when (event.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> handleMoveToForeground(event)
            UsageEvents.Event.MOVE_TO_BACKGROUND -> handleMoveToBackground(event)
        }
    }

    private suspend fun handleMoveToForeground(event: UsageEvents.Event) {
        val packageName = event.packageName ?: return
        if (shouldIgnorePackage(packageName)) {
            return
        }

        sessionTracker.onAppMovedToForeground(packageName, event.timeStamp)

        if (launcherDetector.isLauncherPackage(packageName)) {
            logEvent(
                timestampMillis = event.timeStamp,
                eventType = ActivityEventType.HOME,
                packageName = packageName
            )
        } else {
            logEvent(
                timestampMillis = event.timeStamp,
                eventType = ActivityEventType.APP_OPENED,
                packageName = packageName
            )
        }

        lastForegroundPackage = packageName
    }

    private suspend fun handleMoveToBackground(event: UsageEvents.Event) {
        val packageName = event.packageName ?: return
        if (shouldIgnorePackage(packageName)) {
            return
        }

        val durationSeconds = sessionTracker.onAppMovedToBackground(packageName, event.timeStamp)
        if (launcherDetector.isLauncherPackage(packageName)) {
            return
        }

        logEvent(
            timestampMillis = event.timeStamp,
            eventType = ActivityEventType.APP_CLOSED,
            packageName = packageName,
            durationSeconds = durationSeconds
        )

        if (lastForegroundPackage == packageName) {
            lastForegroundPackage = null
        }
    }

    private suspend fun logEvent(
        timestampMillis: Long,
        eventType: ActivityEventType,
        packageName: String,
        durationSeconds: Long? = null
    ) {
        val appDisplayName = appLabelResolver.resolveAppLabel(packageName)
        activityLogRepository.insertLog(
            ActivityLog(
                timestampMillis = timestampMillis,
                eventType = eventType,
                appPackageName = packageName,
                appDisplayName = appDisplayName,
                durationSeconds = durationSeconds
            )
        )
    }

    private fun shouldIgnorePackage(packageName: String): Boolean {
        return packageName == context.packageName
    }

    companion object {
        private const val POLL_LOOKBACK_MILLIS = 60_000L
    }
}
