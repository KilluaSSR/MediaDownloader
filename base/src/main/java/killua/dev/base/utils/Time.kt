package killua.dev.base.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

@SuppressLint("SimpleDateFormat")
fun isTimeEarlierThan(timestamp: String, targetTime: String): Boolean {
    val timestampSeconds = timestamp.toLong()
    val targetTimestamp = targetTime.toLong()
    return timestampSeconds < targetTimestamp
}

@SuppressLint("SimpleDateFormat")
fun isTimeLaterThan(timestamp: String, targetTime: String): Boolean {
    val timestampSeconds = timestamp.toLong()
    val targetTimestamp = targetTime.toLong()
    return timestampSeconds > targetTimestamp
}

@SuppressLint("SimpleDateFormat")
fun parseTimestamp(timestamp: Long): String {
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}