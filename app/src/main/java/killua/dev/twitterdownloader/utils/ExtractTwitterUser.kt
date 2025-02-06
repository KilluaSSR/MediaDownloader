package killua.dev.twitterdownloader.utils

import killua.dev.twitterdownloader.api.Twitter.Model.RootDto
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser


fun RootDto.extractTwitterUser(): TwitterUser {
    val tweetData = this.data?.threaded_conversation_with_injections_v2?.instructions?.firstOrNull()
        ?.entries?.firstOrNull()?.content?.itemContent?.tweet_results?.result?.let { result ->
            result.tweet ?: result
        }
    val user = tweetData
        ?.core
        ?.user_results
        ?.result

    return TwitterUser(
        id = user?.rest_id,
        screenName = user?.legacy?.screen_name,
        name = user?.legacy?.name,
        createdTime = 0L
    )
}