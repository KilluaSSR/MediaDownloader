package killua.dev.base.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

@SuppressLint("SimpleDateFormat")
fun isTimeEarlierThan(timestamp: String, targetTime: String): Boolean {
    val timestampSeconds = timestamp.toLong() / 1000
    val targetTimestamp = SimpleDateFormat("yyyy-MM-dd").parse(targetTime)?.time?.div(1000) ?: return false
    return timestampSeconds < targetTimestamp
}

@SuppressLint("SimpleDateFormat")
fun isTimeLaterThan(timestamp: String, targetTime: String): Boolean {
    val timestampSeconds = timestamp.toLong() / 1000
    val targetTimestamp = SimpleDateFormat("yyyy-MM-dd").parse(targetTime)?.time?.div(1000) ?: return false
    return timestampSeconds > targetTimestamp
}

@SuppressLint("SimpleDateFormat")
fun parseTimestamp(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd").format(Date(timestamp * 1000))
}