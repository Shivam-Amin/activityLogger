package com.activitylogger.ui.permissions

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitylogger.monitor.MonitorController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PermissionUiState(
    val permissions: List<PermissionStatus> = emptyList(),
    val allGranted: Boolean = false,
    val monitoringActive: Boolean = false
)

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val application: Application,
    private val permissionManager: PermissionManager,
    private val monitorController: MonitorController
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    init {
        refreshPermissionState()
    }

    fun getRuntimePermissionsToRequest(): Array<String> {
        return permissionManager.getRuntimePermissionsToRequest()
    }

    fun openUsageAccessSettings() {
        application.startActivity(permissionManager.createUsageAccessSettingsIntent())
    }

    fun refreshPermissionState() {
        val permissions = permissionManager.getPermissionStatuses()
        val allGranted = permissionManager.areAllRequiredPermissionsGranted()
        val monitoringActive = monitorController.isMonitoringActive()

        _uiState.update {
            it.copy(
                permissions = permissions,
                allGranted = allGranted,
                monitoringActive = monitoringActive
            )
        }

        if (allGranted && !monitoringActive) {
            startMonitoring()
        }
    }

    fun startMonitoring() {
        if (!permissionManager.areAllRequiredPermissionsGranted()) {
            return
        }

        viewModelScope.launch {
            monitorController.startMonitoring(application)
            _uiState.update { state ->
                state.copy(monitoringActive = true)
            }
        }
    }

    fun stopMonitoring() {
        viewModelScope.launch {
            monitorController.stopMonitoring(application)
            _uiState.update { state ->
                state.copy(monitoringActive = false)
            }
        }
    }
}
