import android.os.Build
import androidx.annotation.RequiresApi
import api.Constants.GetTweetDetailFeatures
import api.Constants.TwitterAPIURL
import api.Model.GraphQLResponse
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.core.utils.TweetData
import killua.dev.core.utils.addTwitterHeaders
import killua.dev.core.utils.extractTwitterUser
import killua.dev.core.utils.getAllHighestBitrateUrls
import killua.dev.core.utils.toTweetVariablesSingleMedia
import okhttp3.OkHttpClient
import okhttp3.Request
import repository.LoginCredentials
import java.net.URLEncoder

class TwitterApiService(
    private val client: OkHttpClient,
    private val credentials: LoginCredentials
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getTweetDetailAsync(tweetId: String) {
        if (tweetId.isBlank()) return
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
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) {
                val content = response.body?.string().orEmpty()
                val tweet = gson.fromJson(content, GraphQLResponse::class.java)
                TweetData(
                    user = tweet.extractTwitterUser(),
                    videoUrls = tweet.getAllHighestBitrateUrls()
                )
            } else {
                println("请求失败: ${response.code} - ${response.message}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun buildUrl(baseUrl: String, params: Map<String, String>): String {
        if (params.isEmpty()) return baseUrl
        val query = params.map {
            "${URLEncoder.encode(it.key, Charsets.UTF_8)}=${URLEncoder.encode(it.value, Charsets.UTF_8)}"
        }.joinToString("&")
        return "$baseUrl?$query"
    }
}