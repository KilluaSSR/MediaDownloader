package killua.dev.mediadownloader.utils

import android.util.Log

object StringUtils {
    fun sanitizeFilename(filename: String): String {
        return filename
            .replace("/", "&")
            .replace("|", "&")
            .replace("\\", "&")
            .replace("《", ">")
            .replace("《", ">")
            .replace("：", ":")
            .replace("\"", "")
            .replace("？", "?")
            .replace("*", "·")
            .replace("\n", "")
            .replace("（", "(")
            .replace("）", "(")
    }

    fun String.formatUnicodeToReadable(): String {
        return this
            // 处理 Unicode 转义序列为中文字符
            .replace(Regex("\\\\u([0-9a-fA-F]{4})")) { matchResult ->
                matchResult.groupValues[1].toInt(16).toChar().toString()
            }
            // 处理常见转义字符
            .replace("\\n", System.lineSeparator())
            .also { decoded ->
                // 检查是否成功转换为中文
                if (!decoded.any { it.code > 0x4E00 && it.code < 0x9FFF }) {
                    Log.w("FileUtils", "可能没有正确转换为中文字符")
                }
            }
    }
}