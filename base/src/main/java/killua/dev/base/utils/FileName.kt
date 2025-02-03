package killua.dev.base.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateTwitterVideoFileName(screenName: String?): String {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(Date())
    val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return "${screenName ?: "video"}_${timestamp}_${(1..8).map { chars.random() }.joinToString("")}.mp4"
}