package killua.dev.twitterdownloader.api

import android.os.Build
import androidx.annotation.RequiresApi
import api.Constants.GetTweetDetailFeatures
import api.Constants.TwitterAPIURL
import api.Model.GraphQLResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.base.utils.toTweetVariablesSingleMedia
import killua.dev.twitterdownloader.api.Model.TwitterRequestResult
import killua.dev.twitterdownloader.repository.LoginCredentials
import killua.dev.twitterdownloader.utils.TweetData
import killua.dev.twitterdownloader.utils.addTwitterHeaders
import killua.dev.twitterdownloader.utils.extractTwitterUser
import killua.dev.twitterdownloader.utils.getAllHighestBitrateUrls
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TwitterApiService @Inject constructor(
    private val client: OkHttpClient,
    private val credentials: LoginCredentials
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getTweetDetailAsync(tweetId: String): TwitterRequestResult {
        if (tweetId.isBlank()) return TwitterRequestResult.Error(message = "ID cannot be empty")
        val variables = tweetId.toTweetVariablesSingleMedia()
        val params = mapOf(
            "variables" to gson.toJson(variables),
            "features" to GetTweetDetailFeatures,
            "fieldToggles" to "{\"withArticleRichContentState\":true,\"withArticlePlainText\":false,\"withGrokAnalyze\":false,\"withDisallowedReplyControls\":false}"
        )
        val url = buildUrl(TwitterAPIURL.TweetDetailUrl, params)
        val request = Request.Builder()
            .get()
            .url(url)
            .addTwitterHeaders(credentials.ct0)
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val content = response.body?.string().orEmpty()
                    val tweet = gson.fromJson(content, GraphQLResponse::class.java)
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
}