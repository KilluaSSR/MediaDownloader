package killua.dev.mediadownloader.api.Twitter.BuildRequest

import killua.dev.base.utils.USER_AGENT
import okhttp3.Request

fun <T : Request.Builder> T.addTwitterNormalHeaders(ct0: String): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", "Bearer ${TwitterAPIURL.Bearer}")
        .addHeader("x-csrf-token", ct0)
        .addHeader("x-twitter-active-user", "yes")
        .addHeader("x-twitter-auth-type", "OAuth2Session")
        .addHeader("x-twitter-client-language", "en")
    return this
}

fun <T : Request.Builder> T.addTwitterUserMediaHeaders(ct0: String, screenName: String): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", "Bearer ${TwitterAPIURL.Bearer}")
        .addHeader("Referer", "https://x.com/$screenName")
        .addHeader("x-csrf-token", ct0)
        .addHeader("x-twitter-active-user", "yes")
        .addHeader("x-twitter-auth-type", "OAuth2Session")
        .addHeader("x-twitter-client-language", "en")
    return this
}

fun <T : Request.Builder> T.addTwitterBookmarkHeaders(ct0: String): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "application/json")
        .addHeader("Authorization", "Bearer ${TwitterAPIURL.Bearer}")
        .addHeader("Referer", "https://x.com/i/bookmarks")
        .addHeader("x-csrf-token", ct0)
        .addHeader("x-twitter-active-user", "yes")
        .addHeader("x-twitter-auth-type", "OAuth2Session")
        .addHeader("x-twitter-client-language", "en")
    return this
}