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
        synchronized(labelCache) {
            val cached = labelCache[packageName]
            if (cached != null) return cached

            if (labelCache.size >= MAX_CACHE_SIZE) {
                labelCache.clear()
            }

            val label = runCatching {
                val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationLabel(applicationInfo).toString()
            }.getOrDefault(packageName)

            labelCache[packageName] = label
            return label
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE = 500
    }
}
