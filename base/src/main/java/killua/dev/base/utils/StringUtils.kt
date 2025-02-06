package killua.dev.base.utils

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
}