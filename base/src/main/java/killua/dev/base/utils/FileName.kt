package killua.dev.base.utils

import killua.dev.base.Model.MediaType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface FileNameStrategy {
    fun generate(screenName: String?): String
}

class TwitterMediaFileNameStrategy(
    private val mediaType: MediaType
) : FileNameStrategy {
    override fun generate(screenName: String?): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val randomSuffix = generateRandomSuffix()
        return "${screenName ?: "media"}_${timestamp}_$randomSuffix.${mediaType.extension}"
    }

    private fun generateRandomSuffix(length: Int = 8): String {
        val chars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        return (1..length).map { chars.random() }.joinToString("")
    }
}