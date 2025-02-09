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
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetLikeParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetTwitterBookmarkMediaParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.GetTwitterDownloadSpecificMediaParams
import killua.dev.twitterdownloader.api.Twitter.BuildRequest.TwitterAPIURL
import killua.dev.twitterdownloader.api.Twitter.Model.Bookmark
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarkPageData
import killua.dev.twitterdownloader.api.Twitter.Model.BookmarksPageData
import killua.dev.twitterdownloader.api.Twitter.Model.TweetData
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.di.UserDataManager
import killua.dev.twitterdownloader.utils.addTwitterBookmarkHeaders
import killua.dev.twitterdownloader.utils.addTwitterNormalHeaders
import killua.dev.twitterdownloader.utils.extractBookmarkPageData
import killua.dev.twitterdownloader.utils.extractLikePageData
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
    suspend fun getTwitterSingleMediaDetailAsync(tweetId: String): NetworkResult<TweetData> {
        if (tweetId.isBlank()) return NetworkResult.Error(message = "ID cannot be empty")

        return fetchTwitterPage(
            apiUrl = TwitterAPIURL.TweetDetailUrl,
            params = GetTwitterDownloadSpecificMediaParams(tweetId, gson),
            addHeaders = { it.addTwitterNormalHeaders(userdata.userTwitterData.value.ct0) },
            extractData = { rootDto, _ ->
                TweetData(
                    user = rootDto.extractTwitterUser(),
                    videoUrls = rootDto.getAllHighestBitrateUrls(),
                    photoUrls = rootDto.getAllImageUrls()
                )
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getBookmarksAllTweets(
        onNewBookmarks: suspend (List<Bookmark>) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterBookmarkAsync(cursor) },
        onNewBookmarks = onNewBookmarks
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getLikesAllTweets(
        onNewBookmarks: suspend (List<Bookmark>) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getLikePageAsync(cursor) },
        onNewBookmarks = onNewBookmarks
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getTwitterBookmarkAsync(
        cursor: String,
        count: Int = 20
    ): NetworkResult<BookmarksPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.BookmarkUrl,
        params = GetTwitterBookmarkMediaParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterBookmarkHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractBookmarkPageData(cur) }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getLikePageAsync(
        cursor: String = "",
        count: Int = 50
    ): NetworkResult<BookmarksPageData> = fetchTwitterPage(  // 注意这里改为 BookmarksPageData
        apiUrl = TwitterAPIURL.LikeUrl,
        params = GetLikeParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterNormalHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractLikePageData(cur) }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun <T> fetchTwitterPage(
        apiUrl: String,
        params: Map<String, String>,
        addHeaders: (Request.Builder) -> Request.Builder,
        extractData: (RootDto, String) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .get()
                .url(buildUrl(apiUrl, params))
                .let(addHeaders)
                .build()
                .also {
                    NetworkHelper.setCookies("x.com", mapOf(
                        "ct0" to userdata.userTwitterData.value.ct0,
                        "auth_token" to userdata.userTwitterData.value.auth
                    ))
                }

            NetworkHelper.doRequest(request).use { response ->
                when {
                    response.isSuccessful -> {
                        val content = response.body?.string().orEmpty()
                        val rootDto = try {
                            gson.fromJson(content, RootDto::class.java)
                        } catch (e: Exception) {
                            Log.e("TwitterAPI", "JSON Parse Error", e)
                            return@withContext NetworkResult.Error(message = "JSON解析失败: ${e.message}")
                        }
                        NetworkResult.Success(extractData(rootDto, params["cursor"] ?: ""))
                    }
                    else -> NetworkResult.Error(
                        code = response.code,
                        message = "请求失败: ${response.code} ${response.message}\n${response.body?.string()}"
                    )
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "网络请求失败: ${e.message}")
        }
    }

    private suspend fun fetchAllMediaTweets(
        getPageData: suspend (String) -> NetworkResult<BookmarksPageData>,
        onNewBookmarks: suspend (List<Bookmark>) -> Unit
    ): NetworkResult<TweetData> {
        var currentPage = ""
        val allBookmarks = mutableListOf<Bookmark>()
        var lastUser: TwitterUser? = null
        val processedPages = mutableSetOf<String>()
        try {
            while (true) {
                when (val result = getPageData(currentPage)) {
                    is NetworkResult.Success -> {
                        val pageData = result.data

                        // 添加页面追踪
                        if (currentPage in processedPages) {
                            Log.d("TwitterAPI", "Page already processed: $currentPage")
                            break
                        }
                        processedPages.add(currentPage)

                        val newBookmarks = pageData.bookmark.filter { bookmark ->
                            bookmark.tweetId !in allBookmarks.map { it.tweetId }
                        }

                        if (newBookmarks.isNotEmpty()) {
                            Log.d("TwitterAPI", "Found ${newBookmarks.size} new bookmarks")
                            allBookmarks.addAll(newBookmarks)
                            onNewBookmarks(newBookmarks)
                        }

                        if (lastUser == null) {
                            lastUser = newBookmarks.firstOrNull()?.user
                        }

                        notification.updateBookmarkProgress(
                            photoCount = allBookmarks.sumOf { it.photoUrls.size },
                            videoCount = allBookmarks.sumOf { it.videoUrls.size }
                        )

                        // 改进翻页判断
                        if (pageData.nextPage.isEmpty() ||
                            currentPage == pageData.nextPage ||
                            pageData.nextPage in processedPages) {
                            Log.d("TwitterAPI", "Reached end of pagination")
                            break
                        }

                        currentPage = pageData.nextPage
                        Log.d("TwitterAPI", "Moving to next page: $currentPage")
                        delay(5000)
                    }
                    is NetworkResult.Error -> break
                }
            }
        } catch (e: Exception) {
            // 继续执行，返回已获取的数据
        }

        notification.completeBookmarkProgress(
            totalPhotoCount = allBookmarks.sumOf { it.photoUrls.size },
            totalVideoCount = allBookmarks.sumOf { it.videoUrls.size }
        )

        // 返回总的媒体信息
        return NetworkResult.Success(
            TweetData(
                user = lastUser,
                photoUrls = allBookmarks.flatMap { it.photoUrls }.distinct(),
                videoUrls = allBookmarks.flatMap { it.videoUrls }.distinct()
            )
        )
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

