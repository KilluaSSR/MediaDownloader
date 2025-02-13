package killua.dev.base.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
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
    return SimpleDateFormat("yyyy-MM-dd").format(Date(timestamp * 1000))
}