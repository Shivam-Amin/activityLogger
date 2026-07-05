package com.activitylogger.ui.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class PermissionKind {
    USAGE_ACCESS,
    PHONE,
    CALL_LOG,
    CONTACTS,
    NOTIFICATIONS
}

data class PermissionStatus(
    val kind: PermissionKind,
    val title: String,
    val description: String,
    val isGranted: Boolean,
    val requiresManualSettings: Boolean
)

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun getPermissionStatuses(): List<PermissionStatus> {
        return listOf(
            buildUsageAccessStatus(),
            buildRuntimeStatus(
                kind = PermissionKind.PHONE,
                title = "Phone state",
                description = "Detect when an outgoing call starts and ends.",
                permission = Manifest.permission.READ_PHONE_STATE
            ),
            buildRuntimeStatus(
                kind = PermissionKind.CALL_LOG,
                title = "Call log",
                description = "Read outgoing call duration after the call ends.",
                permission = Manifest.permission.READ_CALL_LOG
            ),
            buildRuntimeStatus(
                kind = PermissionKind.CONTACTS,
                title = "Contacts",
                description = "Show contact names for logged phone numbers.",
                permission = Manifest.permission.READ_CONTACTS
            ),
            buildNotificationStatus()
        )
    }

    fun areAllRequiredPermissionsGranted(): Boolean {
        return getPermissionStatuses().all(PermissionStatus::isGranted)
    }

    fun getRuntimePermissionsToRequest(): Array<String> {
        val permissions = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return permissions
            .filter { permission -> !isRuntimePermissionGranted(permission) }
            .toTypedArray()
    }

    fun hasUsageAccessPermission(): Boolean {
        val appOpsManager = context.getSystemService(AppOpsManager::class.java)
        val mode = appOpsManager.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun createUsageAccessSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun createApplicationDetailsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    private fun buildUsageAccessStatus(): PermissionStatus {
        return PermissionStatus(
            kind = PermissionKind.USAGE_ACCESS,
            title = "Usage access",
            description = "Required to detect app open, close, and home screen events.",
            isGranted = hasUsageAccessPermission(),
            requiresManualSettings = true
        )
    }

    private fun buildNotificationStatus(): PermissionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PermissionStatus(
                kind = PermissionKind.NOTIFICATIONS,
                title = "Notifications",
                description = "Shows the persistent background monitoring notification.",
                isGranted = true,
                requiresManualSettings = false
            )
        }

        return buildRuntimeStatus(
            kind = PermissionKind.NOTIFICATIONS,
            title = "Notifications",
            description = "Shows the persistent background monitoring notification.",
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
    }

    private fun buildRuntimeStatus(
        kind: PermissionKind,
        title: String,
        description: String,
        permission: String
    ): PermissionStatus {
        return PermissionStatus(
            kind = kind,
            title = title,
            description = description,
            isGranted = isRuntimePermissionGranted(permission),
            requiresManualSettings = false
        )
    }

    private fun isRuntimePermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED
    }
}
