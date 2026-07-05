package com.activitylogger.ui.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PermissionScreen(
    onNavigateToLogs: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val runtimePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissionState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPermissionState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Permissions & Monitoring",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Grant the permissions below to start all-day background logging. " +
                "Usage access must be enabled manually in system settings.",
            style = MaterialTheme.typography.bodyMedium
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(uiState.permissions, key = { it.kind.name }) { permission ->
                PermissionCard(permissionStatus = permission)
            }
        }

        Button(
            onClick = {
                val runtimePermissions = viewModel.getRuntimePermissionsToRequest()
                if (runtimePermissions.isNotEmpty()) {
                    runtimePermissionLauncher.launch(runtimePermissions)
                } else {
                    viewModel.refreshPermissionState()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Grant runtime permissions")
        }

        OutlinedButton(
            onClick = { viewModel.openUsageAccessSettings() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open usage access settings")
        }

        if (uiState.allGranted) {
            Text(
                text = if (uiState.monitoringActive) {
                    "Monitoring is running in the background."
                } else {
                    "All permissions granted. Starting monitor..."
                },
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(
                onClick = onNavigateToLogs,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View activity logs")
            }

            OutlinedButton(
                onClick = { viewModel.stopMonitoring() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Stop monitoring")
            }
        }
    }
}

@Composable
private fun PermissionCard(permissionStatus: PermissionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (permissionStatus.isGranted) {
                    Icons.Default.CheckCircle
                } else {
                    Icons.Default.ErrorOutline
                },
                contentDescription = null,
                tint = if (permissionStatus.isGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = permissionStatus.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = permissionStatus.description,
                    style = MaterialTheme.typography.bodySmall
                )
                if (permissionStatus.requiresManualSettings && !permissionStatus.isGranted) {
                    Text(
                        text = "Manual setting required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
