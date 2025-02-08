package killua.dev.twitterdownloader.api.Twitter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import killua.dev.twitterdownloader.api.Twitter.Model.RootDto
import com.google.gson.GsonBuilder
import killua.dev.base.utils.ShowNotification
import killua.dev.twitterdownloader.api.NetworkHelper
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetTwitterBookmarkMediaParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetTwitterDownloadSpecificMediaParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.TwitterAPIURL
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarkPageData
import killua.dev.twitterdownloader.api.Twitter.Model.TweetData
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.di.UserDataManager
import killua.dev.twitterdownloader.utils.addTwitterBookmarkHeaders
import killua.dev.twitterdownloader.utils.addTwitterSingleMediaHeaders
import killua.dev.twitterdownloader.utils.extractTwitterUser
import killua.dev.twitterdownloader.utils.getAllHighestBitrateUrls
import killua.dev.twitterdownloader.utils.getAllImageUrls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.net.URLEncoder
import javax.inject.Inject


class TwitterDownloadAPI @Inject constructor(
    val userdata: UserDataManager,
    private val notification: ShowNotification
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getTwitterSingleMediaDetailAsync(tweetId: String): NetworkResult<TweetData> {
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
            .addTwitterSingleMediaHeaders(userdata.userTwitterData.value.ct0)
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
    suspend fun getBookmarksAllTweets(
        onNewMedia: suspend (TwitterUser?, List<String>, List<String>) -> Unit
    ): NetworkResult<TweetData> {
        var currentPage = ""
        val allPhotoUrls = mutableListOf<String>()
        val allVideoUrls = mutableListOf<String>()
        var user: TwitterUser? = null
        try {
            while (true) {
                when (val result = getTwitterBookmarkAsync(cursor = currentPage)) {
                    is NetworkResult.Success -> {
                        val tweetData = result.data

                        val newPhotoUrls = tweetData.photoUrls.filter { it !in allPhotoUrls }
                        val newVideoUrls = tweetData.videoUrls.filter { it !in allVideoUrls }

                        allPhotoUrls.addAll(newPhotoUrls)
                        allVideoUrls.addAll(newVideoUrls)

                        notification.updateBookmarkProgress(
                            photoCount = allPhotoUrls.size,
                            videoCount = allVideoUrls.size
                        )

                        if (newPhotoUrls.isNotEmpty() || newVideoUrls.isNotEmpty()) {
                            onNewMedia(tweetData.user ?: user, newPhotoUrls, newVideoUrls)
                        }

                        if (user == null) {
                            user = tweetData.user
                        }

                        if (tweetData.nextPage.isEmpty() || currentPage == tweetData.nextPage) {
                            break
                        }
                        currentPage = tweetData.nextPage
                        delay(1000)
                    }
                    is NetworkResult.Error -> {
                        notification.completeBookmarkProgress(
                            totalPhotoCount = allPhotoUrls.distinct().size,
                            totalVideoCount = allVideoUrls.distinct().size
                        )
                        return NetworkResult.Success(
                            TweetData(
                                user = user,
                                photoUrls = allPhotoUrls.distinct(),
                                videoUrls = allVideoUrls.distinct()
                            )
                        )
                    }
                }
            }
            notification.completeBookmarkProgress(
                totalPhotoCount = allPhotoUrls.distinct().size,
                totalVideoCount = allVideoUrls.distinct().size
            )
            return NetworkResult.Success(
                TweetData(
                    user = user,
                    photoUrls = allPhotoUrls.distinct(),
                    videoUrls = allVideoUrls.distinct()
                )
            )
        } catch (e: Exception) {
            notification.completeBookmarkProgress(
                totalPhotoCount = allPhotoUrls.distinct().size,
                totalVideoCount = allVideoUrls.distinct().size
            )
            return NetworkResult.Success(
                TweetData(
                    user = user,
                    photoUrls = allPhotoUrls.distinct(),
                    videoUrls = allVideoUrls.distinct()
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getTwitterBookmarkAsync(cursor: String, count: Int = 20): NetworkResult<BookmarkPageData> {
        return withContext(Dispatchers.IO) {
            val params = GetTwitterBookmarkMediaParams(count, cursor, userdata.userTwitterData.value.twid, gson)
            val url = buildUrl(TwitterAPIURL.BookmarkUrl, params)

            // 记录请求参数
            Log.d("TwitterAPI", "Request URL: $url")
            Log.d("TwitterAPI", "Params: ${gson.toJson(params)}")
            Log.d("TwitterAPI", "User ID: ${userdata.userTwitterData.value.twid}")
            Log.d("TwitterAPI", "CT0: ${userdata.userTwitterData.value.ct0}")

            NetworkHelper.setCookies("x.com", mapOf(
                "ct0" to userdata.userTwitterData.value.ct0,
                "auth_token" to userdata.userTwitterData.value.auth
            ))

            val request = Request.Builder()
                .get()
                .url(url)
                .addTwitterBookmarkHeaders(userdata.userTwitterData.value.ct0)
                .build()

            // 记录请求头
            Log.d("TwitterAPI", "Request Headers:")
            request.headers.forEach { header ->
                Log.d("TwitterAPI", "${header.first}: ${header.second}")
            }

            try {
                NetworkHelper.doRequest(request).use { response ->
                    Log.d("TwitterAPI", "Response Code: ${response.code}")
                    Log.d("TwitterAPI", "Response Message: ${response.message}")

                    when {
                        response.isSuccessful -> {
                            val content = response.body?.string().orEmpty()

                            // 记录响应内容
                            Log.d("TwitterAPI", "Response Content Length: ${content.length}")
                            Log.d("TwitterAPI", "Response Content Preview: ${content.take(500)}...")

                            val rootDto = try {
                                gson.fromJson(content, RootDto::class.java)
                            } catch (e: Exception) {
                                Log.e("TwitterAPI", "JSON Parse Error", e)
                                return@withContext NetworkResult.Error(message = "JSON解析失败: ${e.message}")
                            }

                            // 记录解析后的数据结构
                            Log.d("TwitterAPI", "Root DTO null? ${rootDto == null}")
                            Log.d("TwitterAPI", "Data null? ${rootDto?.data == null}")
                            Log.d("TwitterAPI", "Timeline null? ${rootDto?.data?.bookmark_timeline_v2?.timeline == null}")
                            Log.d("TwitterAPI", "Instructions size: ${rootDto?.data?.bookmark_timeline_v2?.timeline?.instructions?.size ?: 0}")

                            var nextPage = cursor
                            val photoUrls = mutableListOf<String>()
                            val videoUrls = mutableListOf<String>()

                            rootDto.data?.bookmark_timeline_v2?.timeline?.instructions?.forEach { instruction ->
                                Log.d("TwitterAPI", "Instruction type: ${instruction.type}")

                                if (instruction.type == "TimelineAddEntries") {
                                    Log.d("TwitterAPI", "Entries size: ${instruction.entries?.size ?: 0}")

                                    instruction.entries?.forEach { entry ->
                                        Log.d("TwitterAPI", "Entry ID: ${entry.entryId}")

                                        when {
                                            entry.entryId?.startsWith("tweet-") == true -> {
                                                val tweetResult = entry.content?.itemContent?.tweet_results?.result
                                                Log.d("TwitterAPI", "Tweet Result null? ${tweetResult == null}")

                                                tweetResult?.legacy?.extended_entities?.media?.forEach { media ->
                                                    Log.d("TwitterAPI", "Media type: ${media.type}")

                                                    when (media.type) {
                                                        "photo" -> {
                                                            media.media_url_https?.let {
                                                                photoUrls.add(it)
                                                                Log.d("TwitterAPI", "Added photo URL: $it")
                                                            }
                                                        }
                                                        "video" -> {
                                                            val variants = media.video_info?.variants
                                                            Log.d("TwitterAPI", "Video variants size: ${variants?.size ?: 0}")

                                                            media.video_info?.variants
                                                                ?.filter { it.bitrate != null }
                                                                ?.maxByOrNull { it.bitrate!! }
                                                                ?.url?.let {
                                                                    videoUrls.add(it)
                                                                    Log.d("TwitterAPI", "Added video URL: $it")
                                                                }
                                                        }
                                                    }
                                                }
                                            }
                                            entry.entryId?.startsWith("cursor-bottom-") == true -> {
                                                nextPage = entry.content?.value ?: cursor
                                                Log.d("TwitterAPI", "Next page cursor: $nextPage")
                                            }
                                        }
                                    }
                                }
                            }

                            Log.d("TwitterAPI", "Final Results:")
                            Log.d("TwitterAPI", "Photos found: ${photoUrls.size}")
                            Log.d("TwitterAPI", "Videos found: ${videoUrls.size}")
                            Log.d("TwitterAPI", "Next page: $nextPage")

                            NetworkResult.Success(
                                BookmarkPageData(
                                    user = rootDto.extractTwitterUser(),
                                    photoUrls = photoUrls,
                                    videoUrls = videoUrls,
                                    nextPage = nextPage
                                )
                            )
                        }
                        else -> {
                            // 记录错误响应
                            val errorBody = response.body?.string()
                            Log.e("TwitterAPI", "Error Response Body: $errorBody")
                            NetworkResult.Error(
                                code = response.code,
                                message = "请求失败: ${response.code} ${response.message}\n$errorBody"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("TwitterAPI", "Network Request Failed", e)
                NetworkResult.Error(message = "网络请求失败: ${e.message}")
            }
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

