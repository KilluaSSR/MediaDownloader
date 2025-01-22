package killua.dev.twitterdownloader.api

import android.os.Build
import androidx.annotation.RequiresApi
import api.Model.RootDto
import api.Model.extractTwitterUser
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.base.datastore.ApplicationUserData
import killua.dev.twitterdownloader.api.Constants.GetTweetDetailFeatures
import killua.dev.twitterdownloader.api.Constants.TwitterAPIURL
import killua.dev.twitterdownloader.api.Model.TweetData
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.di.UserDataManager
import killua.dev.twitterdownloader.utils.addTwitterHeaders
import killua.dev.twitterdownloader.utils.getAllHighestBitrateUrls
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.CookieManager
import java.net.HttpCookie
import java.net.URI
import java.net.URLEncoder
import javax.inject.Inject


class TwitterApiService @Inject constructor(
    val userdata: UserDataManager
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getTweetDetailAsync(tweetId: String): TwitterRequestResult {
        if (tweetId.isBlank()) return TwitterRequestResult.Error(message = "ID cannot be empty")
        val variables = mapOf(
            "focalTweetId" to tweetId,
            "with_rux_injections" to false,
            "rankingMode" to "Relevance",
            "includePromotedContent" to true,
            "withCommunity" to true,
            "withQuickPromoteEligibilityTweetFields" to true,
            "withBirdwatchNotes" to true,
            "withVoice" to true
        )
        val client = buildClient(userdata.userData.value)
        val params = mapOf(
            "variables" to gson.toJson(variables),
            "features" to GetTweetDetailFeatures,
            "fieldToggles" to "{\"withArticleRichContentState\":true,\"withArticlePlainText\":false,\"withGrokAnalyze\":false,\"withDisallowedReplyControls\":false}"
        )
        val url = buildUrl(TwitterAPIURL.TweetDetailUrl, params)
        val request = Request.Builder()
            .get()
            .url(url)
            .addTwitterHeaders(userdata.userData.value.ct0)
            .build()
        println(request)
        return try {
            client.newCall(request).execute().use { response ->
                println(response.code)
                if (response.isSuccessful) {
                    val content = response.body?.string().orEmpty()
                    val tweet = gson.fromJson(content, RootDto::class.java)
                    val tweetData = TweetData(
                        user = tweet.extractTwitterUser(),
                        videoUrls = tweet.getAllHighestBitrateUrls()
                    )
                    TwitterRequestResult.Success(tweetData)
                } else {
                    TwitterRequestResult.Error(
                        code = response.code,
                        message = "请求失败: ${response.message}"
                    )
                }
            }
        } catch (e: Exception) {
            TwitterRequestResult.Error(message = e.message ?: "未知错误")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildUrl(baseUrl: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseUrl
        val query = params.map {
            "${URLEncoder.encode(it.key, Charsets.UTF_8)}=${
                URLEncoder.encode(
                    it.value,
                    Charsets.UTF_8
                )
            }"
        }.joinToString("&")
        return "$baseUrl?$query"
    }

    fun buildClient(userData: ApplicationUserData): OkHttpClient {
        val cookieManager = CookieManager().apply {
            val ct0Cookie = HttpCookie("ct0", userData.ct0).apply { domain = "x.com" }
            val authCookie =
                HttpCookie("auth_token", userData.auth).apply { domain = "x.com" }
            cookieStore.add(URI("https://x.com"), ct0Cookie)
            cookieStore.add(URI("https://x.com"), authCookie)
        }
        return OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieManager.cookieStore.cookies.map {
                        Cookie.Builder()
                            .name(it.name)
                            .value(it.value)
                            .domain(it.domain)
                            .build()
                    }
                }

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {}
            })
            .build()
    }
}