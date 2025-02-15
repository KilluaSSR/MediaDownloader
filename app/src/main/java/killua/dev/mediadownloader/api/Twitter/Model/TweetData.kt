package killua.dev.mediadownloader.api.Twitter.Model

data class TweetData(
    val user: TwitterUser?,
    val videoUrls: List<String>,
    val photoUrls: List<String>
)

