package com.activitylogger.monitor

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorController @Inject constructor(
    private val monitoringStateStore: MonitoringStateStore
) {

    fun startMonitoring(context: Context) {
        val serviceIntent = MonitorForegroundService.createStartIntent(context)
        ContextCompat.startForegroundService(context, serviceIntent)
        monitoringStateStore.setMonitoringActive(true)
    }

    fun stopMonitoring(context: Context) {
        val serviceIntent = MonitorForegroundService.createStartIntent(context)
        context.stopService(serviceIntent)
        monitoringStateStore.setMonitoringActive(false)
    }

    fun isMonitoringActive(): Boolean {
        return monitoringStateStore.isMonitoringActive()
    }
}
