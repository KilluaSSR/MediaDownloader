package killua.dev.mediadownloader.api.Twitter

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.base.utils.ShowNotification
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.NetworkHelper
import killua.dev.mediadownloader.api.Twitter.BuildRequest.GetLikeParams
import killua.dev.mediadownloader.api.Twitter.BuildRequest.GetTwitterBookmarkMediaParams
import killua.dev.mediadownloader.api.Twitter.BuildRequest.GetTwitterDownloadSpecificMediaParams
import killua.dev.mediadownloader.api.Twitter.BuildRequest.GetUserMediaParams
import killua.dev.mediadownloader.api.Twitter.BuildRequest.GetUserProfileParams
import killua.dev.mediadownloader.api.Twitter.BuildRequest.TwitterAPIURL
import killua.dev.mediadownloader.api.Twitter.BuildRequest.addTwitterBookmarkHeaders
import killua.dev.mediadownloader.api.Twitter.BuildRequest.addTwitterNormalHeaders
import killua.dev.mediadownloader.api.Twitter.BuildRequest.addTwitterUserMediaHeaders
import killua.dev.mediadownloader.api.Twitter.Model.Bookmark
import killua.dev.mediadownloader.api.Twitter.Model.MediaPageData
import killua.dev.mediadownloader.api.Twitter.Model.RootDto
import killua.dev.mediadownloader.api.Twitter.Model.TweetData
import killua.dev.mediadownloader.api.Twitter.Model.TwitterUser
import killua.dev.mediadownloader.api.Twitter.Model.UserBasicInfo
import killua.dev.mediadownloader.api.Twitter.Model.extractMediaPageData
import killua.dev.mediadownloader.api.Twitter.Model.extractTwitterUser
import killua.dev.mediadownloader.api.Twitter.Model.extractUserMediaPageData
import killua.dev.mediadownloader.di.UserDataManager
import killua.dev.mediadownloader.utils.getAllHighestBitrateUrls
import killua.dev.mediadownloader.utils.getAllImageUrls
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
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterBookmarkAsync(cursor) },
        onNewItems = onNewItems,
        onError = onError
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getTwitterBookmarkAsync(
        cursor: String,
        count: Int = 20
    ): NetworkResult<MediaPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.BookmarkUrl,
        params = GetTwitterBookmarkMediaParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterBookmarkHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractMediaPageData(cur, true) }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getLikesAllTweets(
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterLikesAsync(cursor) },
        onNewItems = onNewItems,
        onError = onError
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getTwitterLikesAsync(
        cursor: String,
        count: Int = 20
    ): NetworkResult<MediaPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.LikeUrl,
        params = GetLikeParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterNormalHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractMediaPageData(cur, false) }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getUserMediaAsync(
        userId: String,
        screenName: String,
        cursor: String,
        count: Int = 20
    ): NetworkResult<MediaPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.UserMediaUrl,
        params = GetUserMediaParams(
            userId = userId,
            count = count,
            cursor = cursor,
            gson = gson
        ),
        addHeaders = { it.addTwitterUserMediaHeaders(userdata.userTwitterData.value.ct0, screenName) },
        extractData = { rootDto, cur -> rootDto.extractUserMediaPageData(cur) }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getUserBasicInfo(screenName: String): NetworkResult<UserBasicInfo> =
        getUserIdByScreenNameAsync(screenName.removePrefix("@"))

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun getUserIdByScreenNameAsync(
        screenName: String
    ): NetworkResult<UserBasicInfo> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.ProfileSpotlightsUrl,
        params = GetUserProfileParams(screenName, gson),
        addHeaders = { it.addTwitterNormalHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, _ ->
            UserBasicInfo(
                id = rootDto.data?.user_result_by_screen_name?.result?.rest_id,
                name = rootDto.data?.user_result_by_screen_name?.result?.legacy?.name,
                screenName = rootDto.data?.user_result_by_screen_name?.result?.legacy?.screen_name
            )
        }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getUserMediaByUserId(
        userId: String,
        screenName: String,
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getUserMediaAsync(userId, screenName, cursor) },
        onNewItems = onNewItems,
        onError = onError
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
                    else -> {
                        NetworkResult.Error(
                            code = response.code,
                            message = "请求失败: ${response.code} ${response.message}\n${response.body?.string()}"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            NetworkResult.Error(message = "网络请求失败: ${e.message}")
        }
    }

    private suspend fun fetchAllMediaTweets(
        getPageData: suspend (String) -> NetworkResult<MediaPageData>,
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> {
        var currentPage = ""
        val allItems = mutableListOf<Bookmark>()
        var lastUser: TwitterUser? = null
        val processedPages = mutableSetOf<String>()

        try {
            while (true) {
                when (val result = getPageData(currentPage)) {
                    is NetworkResult.Success -> {
                        val pageData = result.data
                        if (currentPage in processedPages) {
                            Log.d("TwitterAPI", "Page already processed: $currentPage")
                            break
                        }
                        processedPages.add(currentPage)

                        val newItems = pageData.items.filter { item ->
                            item.tweetId !in allItems.map { it.tweetId }
                        }

                        if (newItems.isNotEmpty()) {
                            Log.d("TwitterAPI", "Found ${newItems.size} new items")
                            allItems.addAll(newItems)
                            onNewItems(newItems)
                        }

                        if (lastUser == null) {
                            lastUser = newItems.firstOrNull()?.user
                        }

                        notification.updateGettingTweetsProgress(
                            photoCount = allItems.sumOf { it.photoUrls.size },
                            videoCount = allItems.sumOf { it.videoUrls.size }
                        )

                        if (pageData.nextPage.isEmpty() ||
                            currentPage == pageData.nextPage ||
                            pageData.nextPage in processedPages) {
                            Log.d("TwitterAPI", "Reached end of pagination")
                            break
                        }

                        currentPage = pageData.nextPage
                        Log.d("TwitterAPI", "Moving to next page: $currentPage")
                        delay(userdata.delay.value * 1000L)
                    }
                    is NetworkResult.Error -> {
                        onError(result.message)
                        break
                    }
                }
            }
        } catch (e: Exception) {
            onError("${e.message}")
        }

        notification.completeGettingProgress(
            totalPhotoCount = allItems.sumOf { it.photoUrls.size },
            totalVideoCount = allItems.sumOf { it.videoUrls.size }
        )

        return NetworkResult.Success(
            TweetData(
                user = lastUser,
                photoUrls = allItems.flatMap { it.photoUrls }.distinct(),
                videoUrls = allItems.flatMap { it.videoUrls }.distinct()
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

