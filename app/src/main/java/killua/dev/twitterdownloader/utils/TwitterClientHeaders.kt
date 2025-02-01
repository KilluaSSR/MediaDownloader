package killua.dev.twitterdownloader.utils

import killua.dev.twitterdownloader.api.Constants.TwitterAPIURL
import killua.dev.twitterdownloader.api.Constants.USER_AGENT
import okhttp3.Request

fun <T : Request.Builder> T.addTwitterHeaders(ct0: String): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", "Bearer ${TwitterAPIURL.Bearer}")
        .addHeader("x-csrf-token", ct0)
        .addHeader("x-twitter-active-user", "yes")
        .addHeader("x-twitter-auth-type", "OAuth2Session")
        .addHeader("x-twitter-client-language", "en")
    return this
}