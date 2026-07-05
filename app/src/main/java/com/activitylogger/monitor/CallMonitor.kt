package com.activitylogger.monitor

import android.content.Context
import android.provider.CallLog
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.activitylogger.data.repository.ActivityLogRepository
import com.activitylogger.domain.model.ActivityEventType
import com.activitylogger.domain.model.ActivityLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val activityLogRepository: ActivityLogRepository,
    private val contactResolver: ContactResolver
) {

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isCallActive: Boolean = false
    private var telephonyCallback: TelephonyCallback? = null

    fun register(telephonyManager: TelephonyManager) {
        if (telephonyCallback != null) {
            return
        }

        val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK -> onCallOffHook()
                    TelephonyManager.CALL_STATE_IDLE -> onCallIdle()
                }
            }
        }

        telephonyCallback = callback
        telephonyManager.registerTelephonyCallback(context.mainExecutor, callback)
    }

    fun unregister(telephonyManager: TelephonyManager) {
        telephonyCallback?.let { callback ->
            telephonyManager.unregisterTelephonyCallback(callback)
        }
        telephonyCallback = null
        isCallActive = false
    }

    private fun onCallOffHook() {
        isCallActive = true
    }

    private fun onCallIdle() {
        if (!isCallActive) {
            return
        }
        isCallActive = false
        monitorScope.launch {
            logLatestOutgoingCall()
        }
    }

    private suspend fun logLatestOutgoingCall() {
        val callEntry = fetchLatestOutgoingCall() ?: return

        activityLogRepository.insertLog(
            ActivityLog(
                timestampMillis = callEntry.dateMillis,
                eventType = ActivityEventType.OUTGOING_CALL,
                phoneNumber = callEntry.phoneNumber,
                contactName = callEntry.contactName,
                durationSeconds = callEntry.durationSeconds
            )
        )
    }

    private fun fetchLatestOutgoingCall(): OutgoingCallEntry? {
        val cutoffMillis = System.currentTimeMillis() - CALL_LOOKUP_WINDOW_MILLIS
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
            CallLog.Calls.TYPE
        )
        val selection = "${CallLog.Calls.TYPE} = ? AND ${CallLog.Calls.DATE} >= ?"
        val selectionArgs = arrayOf(
            CallLog.Calls.OUTGOING_TYPE.toString(),
            cutoffMillis.toString()
        )
        val sortOrder = "${CallLog.Calls.DATE} DESC"

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            if (!cursor.moveToFirst()) {
                return null
            }

            val numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val dateIndex = cursor.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION)

            if (numberIndex < 0 || dateIndex < 0 || durationIndex < 0) {
                return null
            }

            val phoneNumber = cursor.getString(numberIndex).orEmpty()
            val dateMillis = cursor.getLong(dateIndex)
            val durationSeconds = cursor.getLong(durationIndex)
            val contactName = contactResolver.resolveContactName(phoneNumber)

            return OutgoingCallEntry(
                phoneNumber = phoneNumber,
                dateMillis = dateMillis,
                durationSeconds = durationSeconds,
                contactName = contactName
            )
        }

        return null
    }

    private data class OutgoingCallEntry(
        val phoneNumber: String,
        val dateMillis: Long,
        val durationSeconds: Long,
        val contactName: String?
    )

    companion object {
        private const val CALL_LOOKUP_WINDOW_MILLIS = 120_000L
    }
}
