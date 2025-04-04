package killua.dev.mediadownloader.Model

data class DownloadTask(
    val id: String,
    val url: String,
    val from: AvailablePlatforms = AvailablePlatforms.Twitter,
    val type: MediaType = MediaType.VIDEO,
    val refererNecessary: String? = null,
    val fileName: String,
    val screenName: String,
    val destinationFolder: String = type.buildPath(from),
    val headers: Map<String, String> = mapOf(),
    val cookies: Map<String, String> = mapOf()
)