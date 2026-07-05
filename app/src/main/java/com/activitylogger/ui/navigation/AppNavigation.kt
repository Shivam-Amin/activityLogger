package com.activitylogger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.activitylogger.ui.logs.LogsScreen
import com.activitylogger.ui.permissions.PermissionScreen

object AppRoutes {
    const val PERMISSIONS = "permissions"
    const val LOGS = "logs"
}

@Composable
fun ActivityLoggerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppRoutes.PERMISSIONS
    ) {
        composable(AppRoutes.PERMISSIONS) {
            PermissionScreen(
                onNavigateToLogs = {
                    navController.navigate(AppRoutes.LOGS)
                }
            )
        }

        composable(AppRoutes.LOGS) {
            LogsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
