package com.activitylogger.monitor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.activitylogger.ui.permissions.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var monitorController: MonitorController

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        if (permissionManager.areAllRequiredPermissionsGranted()) {
            monitorController.startMonitoring(context)
        }
    }
}
