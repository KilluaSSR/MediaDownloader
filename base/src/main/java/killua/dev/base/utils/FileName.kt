package killua.dev.base.utils

import killua.dev.base.Model.MediaType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface FileNameStrategy {
    fun generateMedia(screenName: String?): String
    fun generateManga(title: String, chapter: String): String
}

class MediaFileNameStrategy(
    private val mediaType: MediaType
) : FileNameStrategy {
    override fun generateMedia(screenName: String?): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val randomSuffix = generateRandomSuffix()
        return "${screenName ?: "media"}_${timestamp}_$randomSuffix.${mediaType.extension}"
    }

    override fun generateManga(title: String, chapter: String): String {
        return "${title}_$chapter.${mediaType.extension}"
    }

    private fun generateRandomSuffix(length: Int = 8): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length).map { chars.random() }.joinToString("")
    }
}