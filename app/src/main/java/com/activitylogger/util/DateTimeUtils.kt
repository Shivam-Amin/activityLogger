package com.activitylogger.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateTimeUtils {

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

    fun formatDate(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(dateFormatter)
    }

    fun formatTime(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(timeFormatter)
    }

    fun formatDateTime(timestampMillis: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        return Instant.ofEpochMilli(timestampMillis)
            .atZone(zoneId)
            .format(dateTimeFormatter)
    }

    fun formatDuration(durationSeconds: Long): String {
        val hours = durationSeconds / 3_600
        val minutes = (durationSeconds % 3_600) / 60
        val seconds = durationSeconds % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun startOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return date.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    fun endOfDayMillis(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return date.atTime(LocalTime.MAX).atZone(zoneId).toInstant().toEpochMilli()
    }

    fun today(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
        return LocalDate.now(zoneId)
    }
}
