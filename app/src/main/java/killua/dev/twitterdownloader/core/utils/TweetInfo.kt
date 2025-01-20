package killua.dev.twitterdownloader.core.utils

import api.Model.GraphQLResponse
import killua.dev.twitterdownloader.api.Model.TwitterUser
data class TweetData(
    val user: TwitterUser?,
    val videoUrls: List<String>
)
fun GraphQLResponse.extractTwitterUser(): TwitterUser? {
    val instructions = data.threadedConversationV2?.instructions ?: return null
    val firstEntry = instructions
        .firstOrNull { it.type == "TimelineAddEntries" }
        ?.entries
        ?.firstOrNull() ?: return null

    val tweetResult = firstEntry.content.itemContent?.tweetResults?.result

    val actualTweet = when(tweetResult?.typeName) {
        "TweetWithVisibilityResults" -> tweetResult.tweet
        else -> tweetResult
    }

    val userResult = actualTweet?.core?.userResults?.result ?: return null
    val userLegacy = userResult.legacy

    return TwitterUser(
        id = userResult.id,
        screenName = userLegacy.screenName,
        name = userLegacy.name,
        description = userLegacy.description,
        createdTime = actualTweet.legacy?.createdAt?.let {
            System.currentTimeMillis()
        } ?: 0L
    )
}