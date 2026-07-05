package com.activitylogger.monitor

import android.content.Context
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLabelResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val packageManager: PackageManager = context.packageManager
    private val labelCache = mutableMapOf<String, String>()

    fun resolveAppLabel(packageName: String): String {
        return labelCache.getOrPut(packageName) {
            runCatching {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            }.getOrDefault(packageName)
        }
    }
}
