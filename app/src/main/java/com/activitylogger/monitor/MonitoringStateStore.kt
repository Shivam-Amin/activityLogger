package com.activitylogger.monitor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoringStateStore @Inject constructor(
    @ApplicationContext context: Context
) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun setMonitoringActive(isActive: Boolean) {
        preferences.edit().putBoolean(KEY_MONITORING_ACTIVE, isActive).apply()
    }

    fun isMonitoringActive(): Boolean {
        return preferences.getBoolean(KEY_MONITORING_ACTIVE, false)
    }

    companion object {
        private const val PREFS_NAME = "monitor_state"
        private const val KEY_MONITORING_ACTIVE = "monitoring_active"
    }
}
