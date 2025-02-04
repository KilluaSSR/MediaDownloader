package killua.dev.twitterdownloader.api.Model

import api.Model.TwitterUser

data class TweetData(
    val user: TwitterUser?,
    val videoUrls: List<String>,
    val photoUrls: List<String>
)

