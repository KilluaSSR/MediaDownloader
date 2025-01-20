package killua.dev.twitterdownloader.api.Model

import killua.dev.twitterdownloader.core.utils.TweetData

sealed class TwitterRequestResult {
    data class Success(
        val data: TweetData
    ) : TwitterRequestResult()

    data class Error(
        val code: Int = 0,
        val message: String
    ) : TwitterRequestResult()
}