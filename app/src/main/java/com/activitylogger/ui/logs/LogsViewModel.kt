package com.activitylogger.ui.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.activitylogger.data.repository.ActivityLogRepository
import com.activitylogger.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class LogsUiState(
    val selectedDate: LocalDate = DateTimeUtils.today(),
    val logs: List<LogItemUiModel> = emptyList(),
    val selectedLogIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val activityLogRepository: ActivityLogRepository
) : ViewModel() {

    private val selectedDateFlow = MutableStateFlow(DateTimeUtils.today())
    private val selectionStateFlow = MutableStateFlow(SelectionState())

    private val logsFlow = selectedDateFlow.flatMapLatest { selectedDate ->
        val startMillis = DateTimeUtils.startOfDayMillis(selectedDate)
        val endMillis = DateTimeUtils.endOfDayMillis(selectedDate)
        activityLogRepository.observeLogsBetween(startMillis, endMillis).map { logs ->
            logs.map(LogItemUiMapper::toUiModel)
        }
    }

    val uiState: StateFlow<LogsUiState> = combine(
        selectedDateFlow,
        logsFlow,
        selectionStateFlow
    ) { selectedDate, logs, selectionState ->
        LogsUiState(
            selectedDate = selectedDate,
            logs = logs,
            selectedLogIds = selectionState.selectedLogIds,
            isSelectionMode = selectionState.isSelectionMode,
            message = selectionState.message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LogsUiState()
    )

    fun onDateSelected(date: LocalDate) {
        selectedDateFlow.value = date
        clearSelection()
    }

    fun toggleSelectionMode() {
        selectionStateFlow.update { state ->
            if (state.isSelectionMode) {
                SelectionState()
            } else {
                state.copy(isSelectionMode = true, message = null)
            }
        }
    }

    fun toggleLogSelection(logId: Long) {
        selectionStateFlow.update { state ->
            val updatedSelection = state.selectedLogIds.toMutableSet()
            if (updatedSelection.contains(logId)) {
                updatedSelection.remove(logId)
            } else {
                updatedSelection.add(logId)
            }
            state.copy(selectedLogIds = updatedSelection, message = null)
        }
    }

    fun deleteSelectedLogs() {
        val selectedIds = selectionStateFlow.value.selectedLogIds.toList()
        if (selectedIds.isEmpty()) {
            publishMessage("Select at least one log to delete.")
            return
        }

        viewModelScope.launch {
            val deletedCount = activityLogRepository.deleteLogsByIds(selectedIds)
            clearSelection()
            publishMessage("Deleted $deletedCount log(s).")
        }
    }

    fun deleteLogsForSelectedDate() {
        val selectedDate = selectedDateFlow.value
        viewModelScope.launch {
            val deletedCount = activityLogRepository.deleteLogsForDay(
                startOfDayMillis = DateTimeUtils.startOfDayMillis(selectedDate),
                endOfDayMillis = DateTimeUtils.endOfDayMillis(selectedDate)
            )
            clearSelection()
            publishMessage("Deleted $deletedCount log(s) for ${DateTimeUtils.formatDate(DateTimeUtils.startOfDayMillis(selectedDate))}.")
        }
    }

    private fun clearSelection() {
        selectionStateFlow.value = SelectionState()
    }

    private fun publishMessage(message: String) {
        selectionStateFlow.update { state ->
            state.copy(message = message)
        }
    }

    private data class SelectionState(
        val selectedLogIds: Set<Long> = emptySet(),
        val isSelectionMode: Boolean = false,
        val message: String? = null
    )
}
