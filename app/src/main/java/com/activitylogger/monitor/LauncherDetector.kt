package com.activitylogger.monitor

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val launcherPackages: Set<String> by lazy { resolveLauncherPackages() }

    fun isLauncherPackage(packageName: String): Boolean {
        return launcherPackages.contains(packageName)
    }

    private fun resolveLauncherPackages(): Set<String> {
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        return context.packageManager
            .queryIntentActivities(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
            .map { activityInfo -> activityInfo.activityInfo.packageName }
            .toSet()
    }
}
