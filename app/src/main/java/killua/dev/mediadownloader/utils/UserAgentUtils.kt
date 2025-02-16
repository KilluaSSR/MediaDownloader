package killua.dev.mediadownloader.utils

const val USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0"

object UserAgentUtils {
    fun getHeaders(): Map<String, String> = mapOf(
        "User-Agent" to USER_AGENT
    )

    fun makeLofterHeaders(authorUrl: String): Map<String, String> = mapOf(
        "User-Agent" to USER_AGENT,
        "Host" to authorUrl.split("//")[1].replace("/", ""),
        "Origin" to authorUrl,
        "Referer" to "$authorUrl/view"
    )
}