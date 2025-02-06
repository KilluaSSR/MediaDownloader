package killua.dev.twitterdownloader.api.Twitter

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import killua.dev.twitterdownloader.api.Twitter.Model.RootDto
import com.google.gson.GsonBuilder
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetTwitterDownloadSpecificMediaParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.TwitterAPIURL
import killua.dev.twitterdownloader.api.Twitter.Model.TweetData
import killua.dev.twitterdownloader.di.UserDataManager
import killua.dev.twitterdownloader.utils.addTwitterHeaders
import killua.dev.twitterdownloader.utils.extractTwitterUser
import killua.dev.twitterdownloader.utils.getAllHighestBitrateUrls
import killua.dev.twitterdownloader.utils.getAllImageUrls
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject


class TwitterDownloadSingleMedia @Inject constructor(
    val userdata: UserDataManager,
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getTweetDetailAsync(tweetId: String): NetworkResult<TweetData> {
        if (tweetId.isBlank()) return NetworkResult.Error(message = "ID cannot be empty")
        val params = GetTwitterDownloadSpecificMediaParams(tweetId, gson)
        val url = buildUrl(TwitterAPIURL.TweetDetailUrl, params)
        NetworkHelper.setCookies("x.com", mapOf(
            "ct0" to userdata.userTwitterData.value.ct0,
            "auth_token" to userdata.userTwitterData.value.auth
        ))
        val request = Request.Builder()
            .get()
            .url(url)
            .addTwitterHeaders(userdata.userTwitterData.value.ct0)
            .build()
        println(request)
        return try {
            NetworkHelper.doRequest(request).use { response ->
                when {
                    response.isSuccessful -> {
                        val content = response.body?.string().orEmpty()
                        val tweet = gson.fromJson(content, RootDto::class.java)
                        NetworkResult.Success(TweetData(
                            user = tweet.extractTwitterUser(),
                            videoUrls = tweet.getAllHighestBitrateUrls(),
                            photoUrls = tweet.getAllImageUrls()
                        ))
                    }
                    else -> NetworkResult.Error(
                        code = response.code,
                        message = "Bad request: ${response.message}"
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = e.message ?: "Internal Error")
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