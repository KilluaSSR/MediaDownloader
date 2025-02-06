package killua.dev.twitterdownloader.Model

data class LofterParseRequiredInformation(
    val archiveURL: String,
    val authorURL: String,
    val authorID: String,
    val authorName: String,
    val authorDomain: String,
    val cookies: Map<String, String>,
    val header: Map<String, String>,
    val data: Map<String, String>,
    val startTime: String?,
    val endTime: String?
)

data class AuthorInfo(
    val authorId: String,
    val authorDomain: String,
    val authorName: String
)