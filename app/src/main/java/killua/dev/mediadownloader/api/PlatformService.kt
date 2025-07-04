package killua.dev.mediadownloader.api

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import killua.dev.mediadownloader.Model.ImageType
import killua.dev.mediadownloader.Model.LofterParseRequiredInformation
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.Kuaikan.BuildRequest.KuaikanSingleChapterRequest
import killua.dev.mediadownloader.api.Kuaikan.BuildRequest.KuaikanWholeComicRequest
import killua.dev.mediadownloader.api.Kuaikan.Model.MangaInfo
import killua.dev.mediadownloader.api.Lofter.BuildRequest.makeArchiveData
import killua.dev.mediadownloader.api.Lofter.Model.BlogImage
import killua.dev.mediadownloader.api.Lofter.Model.BlogInfo
import killua.dev.mediadownloader.api.Lofter.Model.extractLofterUserDomain
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseArchivePage
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseAuthorInfo
import killua.dev.mediadownloader.api.Lofter.utils.LofterParser.parseFromArchiveInfos
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.MissEvanGetDramaListRequest
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.MissEvanGetSoundRequest
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.addMissEvanDramaFetchHeaders
import killua.dev.mediadownloader.api.MissEvan.BuildRequest.addMissEvanSoundFetchHeaders
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDramaResult
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDramaSoundResponse
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanEntireDramaResponse
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanSoundResponse
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestEntireNovelURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesDetailsURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestPicturesURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivRequestSingleNovelURL
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivEntireNovelHeaders
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivNovelFetchHeaders
import killua.dev.mediadownloader.api.Pixiv.BuildRequest.addPixivPictureFetchHeaders
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivBlogInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivEntireNovelDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivImageInfo
import killua.dev.mediadownloader.api.Pixiv.Model.PixivNovelDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPictureDetailResponse
import killua.dev.mediadownloader.api.Pixiv.Model.PixivPicturePageResponse
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
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.features.UserDataManager
import killua.dev.mediadownloader.utils.ShowNotification
import killua.dev.mediadownloader.utils.USER_AGENT
import killua.dev.mediadownloader.utils.UserAgentUtils
import killua.dev.mediadownloader.utils.getAllHighestBitrateUrls
import killua.dev.mediadownloader.utils.getAllImageUrls
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import javax.inject.Inject

data class KuaikanChapter(val name: String, val id: String)
data class KuaikanNuxtParams(val returnObject: String, val parameters: String)

class PlatformService @Inject constructor(
    val userdata: UserDataManager,
    @ApplicationScope private val scope: CoroutineScope,
    private val notification: ShowNotification
){
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun getPixivNovel(id: String): NetworkResult<PixivBlogInfo> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestSingleNovelURL(id))
                    .addPixivNovelFetchHeaders(id)
                    .build()
                    .also {
                        NetworkHelper.setCookies("www.pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivNovelDetailResponse::class.java).body
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            return@withContext NetworkResult.Success(detailResult)

        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }

    }

    suspend fun getEntirePixivNovel(id: String): NetworkResult<List<NovelInfo>> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestEntireNovelURL(id))
                    .also {
                        NetworkHelper.setCookies("www.pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value,
                        ))
                    }
                    .addPixivEntireNovelHeaders(id)
                    .build()

            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivEntireNovelDetailResponse::class.java).body
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            return@withContext NetworkResult.Success(detailResult.thumbnails.novel)
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

    suspend fun getSinglePixivBlogImage(id: String): NetworkResult<PixivImageInfo> = withContext(Dispatchers.IO) {
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestPicturesDetailsURL(id))
                    .addPixivPictureFetchHeaders(id)
                    .build()
                    .also {
                        NetworkHelper.setCookies("pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), PixivPictureDetailResponse::class.java).body
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }

            val urls = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(PixivRequestPicturesURL(id))
                    .addPixivPictureFetchHeaders(id)
                    .build()
                    .also {
                        NetworkHelper.setCookies("pixiv.net", mapOf(
                            "PHPSESSID" to userdata.userPixivPHPSSID.value
                        ))
                    }
            ).use { response ->
                if (!response.isSuccessful) {

                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "图片URL请求失败: ${response.code} ${response.message}"
                    )
                }

                try {
                    gson.fromJson(response.body?.string(), PixivPicturePageResponse::class.java)
                        .body
                        .map { it.urls.original }
                } catch (e: Exception) {

                    return@withContext NetworkResult.Error(message = "URL列表JSON解析失败: ${e.message}")
                }
            }

            return@withContext NetworkResult.Success(
                PixivImageInfo(
                    userName = detailResult.userName,
                    userId = detailResult.userId,
                    title = detailResult.title,
                    illustId = detailResult.illustId,
                    originalUrls = urls
                )
            )
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

    suspend fun getSingleChapter(url: String): NetworkResult<MangaInfo> = withContext(Dispatchers.IO) {
        val pattern = """(?:comics/|comic-next/|comic/)(\d+)""".toRegex()

        val id = try {
            pattern.find(url)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("URL中未找到数字ID: $url")
        } catch (e: Exception) {
            val errorMessage = "URL解析失败: ${e.message}"
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
        try {
            NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(KuaikanSingleChapterRequest(id))
                    .header("User-Agent", USER_AGENT)
                    .also {
                        NetworkHelper.setCookies("www.kuaikanmanhua.com", mapOf(
                            "passToken" to userdata.userKuaikanData.value
                        ))
                    }.build()
            ).use { response ->
                if (!response.isSuccessful) {
                    val errorMessage = "详情请求失败: ${response.code} ${response.message}"
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = errorMessage
                    )
                }
                val htmlContent = response.body?.string()
                    ?: return@withContext NetworkResult.Error(message = "响应内容为空")
                val (title, chapter) = extractSingleMangaInfo(htmlContent)
                    ?: return@withContext NetworkResult.Error(message = "无法提取标题和章节信息")
                val imageUrls = extractSingleImageUrls(htmlContent)
                if (imageUrls.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到图片URL")
                }

                NetworkResult.Success(MangaInfo(title, url, chapter, imageUrls))
            }
        } catch (e: Exception) {
            val errorMessage = "请求处理失败: ${e.message}"
            e.printStackTrace()
            NetworkResult.Error(message = errorMessage)
        }
    }

    suspend fun getEntireComic(url: String): NetworkResult<List<KuaikanChapter>> = withContext(Dispatchers.IO) {
        val pattern = """(?:topic|mobile)/(\d+)(?:/list)?(?:[/?].*)?$""".toRegex()

        val id = try {
            pattern.find(url)?.groupValues?.get(1)
                ?: throw IllegalArgumentException("URL中未找到漫画ID: $url")
        } catch (e: Exception) {
            val errorMessage = "URL解析失败: ${e.message}"
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
        try {
            NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(KuaikanWholeComicRequest(id))
                    .header("User-Agent", USER_AGENT)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }

                val htmlContent = response.body?.string()
                    ?: return@withContext NetworkResult.Error(message = "响应内容为空")

                val result = extractNuxtParams(htmlContent)
                if (result == null) {
                    return@withContext NetworkResult.Error(message = "无法提取NUXT参数")
                }

                val chapters = extractChapterInfo(result.parameters)
                if (chapters.isEmpty()) {
                    return@withContext NetworkResult.Error(message = "未找到任何章节信息")
                }

                return@withContext NetworkResult.Success(chapters)
            }
        } catch (e: Exception) {
            val errorMessage = "请求处理失败: ${e.message}"
            e.printStackTrace()
            return@withContext NetworkResult.Error(message = errorMessage)
        }
    }

    fun extractChapterInfo(paramsStr: String): List<KuaikanChapter> {
        val pattern = """"id":(\d{6}),"title":"([^"]+)""""
        val regex = Regex(pattern)

        val kuaikanChapters = mutableListOf<KuaikanChapter>()
        regex.findAll(paramsStr).forEach { matchResult ->
            val chapterId = matchResult.groupValues[1]
            val chapterName = matchResult.groupValues[2]
            kuaikanChapters.add(KuaikanChapter(chapterName, chapterId))
        }

        if (kuaikanChapters.isEmpty()) {
            val backupPattern = """\.jpg","[^"]+","[^"]+",(\d{6}),"([^"]+)""""
            Regex(backupPattern).findAll(paramsStr).forEach { matchResult ->
                val chapterId = matchResult.groupValues[1]
                val chapterName = matchResult.groupValues[2]
                kuaikanChapters.add(KuaikanChapter(chapterName, chapterId))
            }
        }

        return kuaikanChapters.sortedBy { chapter ->
            Regex("""第(\d+)话""").find(chapter.name)
                ?.groupValues?.get(1)?.toIntOrNull() ?: 999
        }
    }

    fun extractNuxtParams(htmlContent: String): KuaikanNuxtParams? {

        try {
            // 1. 提取NUXT内容
            val nuxtStartIndex = htmlContent.indexOf("window.__NUXT__=")
            if (nuxtStartIndex == -1) {
                return null
            }

            val nuxtEndIndex = htmlContent.indexOf(";</script>", nuxtStartIndex)
            if (nuxtEndIndex == -1) {
                return null
            }

            val nuxtContent = htmlContent.substring(
                nuxtStartIndex + "window.__NUXT__=".length,
                nuxtEndIndex
            ).trim()

            // 2. 提取return对象
            val returnStartIndex = nuxtContent.indexOf("return") + "return".length
            val returnEndIndex = nuxtContent.lastIndexOf("}")
            if (returnStartIndex == -1 || returnEndIndex == -1) {
                return null
            }

            val returnObject = nuxtContent.substring(returnStartIndex, returnEndIndex + 1).trim()

            // 3. 提取函数参数
            val paramsStartIndex = nuxtContent.lastIndexOf("(") + 1
            val paramsEndIndex = nuxtContent.lastIndexOf(")")
            if (paramsStartIndex <= 0 || paramsEndIndex <= paramsStartIndex) {
                return null
            }

            val parameters = nuxtContent.substring(paramsStartIndex, paramsEndIndex)


            return KuaikanNuxtParams(returnObject, parameters)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun extractSingleImageUrls(htmlContent: String): List<String> {
        val scriptPattern = """window\.__NUXT__=(.*?)</script>""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val scriptContent = scriptPattern.find(htmlContent)?.groupValues?.get(1)

        if (scriptContent == null) {
            return emptyList()
        }

        val urls = scriptContent
            .split(',')
            .filter { it.contains("http") && it.contains("webp-t.w") && it.contains("jpg.h?sign") }
            .map { it.trim('"').replace("\\u002F", "/") }
        return urls
    }

    private fun extractSingleMangaInfo(htmlContent: String): Pair<String, String>? {
        // 使用正则表达式匹配标题和章节
        val titlePattern = """class="step-topic"[^>]*>([^<]+)""".toRegex()
        val chapterPattern = """class="step-comic"[^>]*>([^<]+)""".toRegex()

        val title = titlePattern.find(htmlContent)?.groupValues?.get(1)?.trim()
        val chapter = chapterPattern.find(htmlContent)?.groupValues?.get(1)?.trim()
        return if (title != null && chapter != null) {
            Pair(title, chapter)
        } else null
    }

    fun getLofterBlogImages(blogUrl: String): NetworkResult<BlogInfo>  {
        val loginKey = userdata.userLofterData.value.login_key
        val loginAuth = userdata.userLofterData.value.login_auth
        val deferred = CompletableDeferred<NetworkResult<BlogInfo>>()
        scope.launch{
            try {
                val blogContent = NetworkHelper.get(
                    url = blogUrl,
                    headers = UserAgentUtils.getHeaders(),
                    cookies = mapOf(loginKey to loginAuth)
                ) { it.decodeToString() }.let { result ->
                    when (result) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> throw Exception("Failed: ${result.message}")
                    }
                }
                val authorViewUrl = "${blogUrl.split("/post")[0]}/view"
                val authorViewContent = NetworkHelper.get(
                    url = authorViewUrl,
                    headers = UserAgentUtils.getHeaders(),
                    cookies = mapOf(loginKey to loginAuth)
                ) { it.decodeToString() }.let { result ->
                    when (result) {
                        is NetworkResult.Success -> result.data
                        is NetworkResult.Error -> throw Exception("Failed:  ${result.message}")
                    }
                }
                val (authorName, authorId) = parseAuthorInfo(authorViewContent)
                val authorDomain = blogUrl.extractLofterUserDomain()
                val imagesUrl = LofterParser.parseImages(blogContent)
                val blogInfo = BlogInfo(
                    authorName = authorName,
                    authorId = authorId,
                    authorDomain = authorDomain!!,
                    images = imagesUrl.mapIndexed { index, imageUrl ->
                        BlogImage(
                            url = imageUrl,
                            filename = LofterParser.generateFilename(
                                authorName = authorName,
                                authorDomain = authorDomain,
                                index = index + 1,
                                imageUrl = imageUrl
                            ),
                            type = ImageType.fromUrl(imageUrl),
                            blogUrl = blogUrl
                        )
                    }
                )
                deferred.complete(NetworkResult.Success(blogInfo))
            }catch (e: Exception) {
                deferred.complete(NetworkResult.Error(message = e.message ?: "Failed"))
            }
        }
        return runBlocking { deferred.await() }
    }


    suspend fun getLofterByAuthorTags(
        authorUrl: String,
        tags: Set<String>,
        saveNoTags: Boolean = false
    ): BlogInfo {

        val loginKey = userdata.userLofterData.value.login_key
        val loginAuth = userdata.userLofterData.value.login_auth
        val startTime = userdata.userLofterData.value.start_time.toString()
        val endTime = userdata.userLofterData.value.end_time.toString()

        val pageContent = try {
            when (val result = NetworkHelper.get(
                url = authorUrl + "view",
                headers = UserAgentUtils.getHeaders(),
                cookies = mapOf(loginKey to loginAuth)
            ) { String(it, Charsets.UTF_8) }) {
                is NetworkResult.Success -> {
                    result.data
                }
                is NetworkResult.Error -> {
                    throw IllegalStateException("Failed to fetch author page")
                }
            }
        } catch (e: Exception) {
            throw e
        }

        val document = Jsoup.parse(pageContent)
        val authorInfo = parseAuthorInfo(document, authorUrl)

        val archiveUrl = "${authorUrl}dwr/call/plaincall/ArchiveBean.getArchivePostByTime.dwr"

        val data = makeArchiveData(authorInfo.authorId, 50)
        val header = UserAgentUtils.makeLofterHeaders(authorUrl)
        val requiredInfo = LofterParseRequiredInformation(
            archiveURL = archiveUrl,
            authorID = authorInfo.authorId,
            authorURL = authorUrl,
            authorName = authorInfo.authorName,
            authorDomain = authorInfo.authorDomain,
            cookies = mapOf(loginKey to loginAuth),
            header = header,
            data = data,
            startTime = startTime,
            endTime = endTime
        )
        val blogInfos = parseArchivePage(requiredInfo)
        return parseFromArchiveInfos(blogInfos, requiredInfo, tags, saveNoTags)
    }

    suspend fun getMissEvanEntireDrama(id: String): NetworkResult<MissEvanDramaResult> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(MissEvanGetDramaListRequest(id))
                    .addMissEvanDramaFetchHeaders(id)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), MissEvanEntireDramaResponse::class.java)
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }
            val (title, author) = Pair(detailResult.info.drama.name, detailResult.info.drama.author)
            val dramaList = detailResult.info.episodes.episode.toList()
            return@withContext NetworkResult.Success(MissEvanDramaResult(title, author, dramaList))
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

    suspend fun getMissEvanDrama(id: String): NetworkResult<MissEvanSoundResponse> = withContext(Dispatchers.IO){
        try {
            val detailResult = NetworkHelper.doRequest(
                Request.Builder()
                    .get()
                    .url(MissEvanGetSoundRequest(id))
                    .also {
                        NetworkHelper.setCookies("www.missevan.com", mapOf(
                            "token" to userdata.userMissEvanData.value,
                        ))
                    }
                    .addMissEvanSoundFetchHeaders(id)
                    .build()
            ).use { response ->
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error(
                        code = response.code,
                        message = "详情请求失败: ${response.code} ${response.message}"
                    )
                }
                try {
                    gson.fromJson(response.body?.string(), MissEvanDramaSoundResponse::class.java).info.sound
                } catch (e: Exception) {
                    return@withContext NetworkResult.Error(message = "详情JSON解析失败: ${e.message}")
                }
            }

            return@withContext NetworkResult.Success(detailResult)
        } catch (e: Exception) {
            NetworkResult.Error(message = "请求过程发生错误: ${e.message}")
        }
    }

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
    suspend fun getBookmarksAllTweets(
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterBookmarkAsync(cursor) },
        onNewItems = onNewItems,
        onError = onError
    )

    private suspend fun getTwitterBookmarkAsync(
        cursor: String,
        count: Int = 20
    ): NetworkResult<MediaPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.BookmarkUrl,
        params = GetTwitterBookmarkMediaParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterBookmarkHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractMediaPageData(cur, true) }
    )

    suspend fun getLikesAllTweets(
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterLikesAsync(cursor) },
        onNewItems = onNewItems,
        onError = onError
    )

    private suspend fun getTwitterLikesAsync(
        cursor: String,
        count: Int = 20
    ): NetworkResult<MediaPageData> = fetchTwitterPage(
        apiUrl = TwitterAPIURL.LikeUrl,
        params = GetLikeParams(count, cursor, userdata.userTwitterData.value.twid, gson),
        addHeaders = { it.addTwitterNormalHeaders(userdata.userTwitterData.value.ct0) },
        extractData = { rootDto, cur -> rootDto.extractMediaPageData(cur, false) }
    )

    private suspend fun getTwitterUserMediaAsync(
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

    suspend fun getTwitterUserBasicInfo(screenName: String): NetworkResult<UserBasicInfo> =
        getTwitterUserIdByScreenNameAsync(screenName.removePrefix("@"))

    private suspend fun getTwitterUserIdByScreenNameAsync(
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

    suspend fun getTwitterUserMediaByUserId(
        userId: String,
        screenName: String,
        onNewItems: suspend (List<Bookmark>) -> Unit,
        onError: (String) -> Unit
    ): NetworkResult<TweetData> = fetchAllMediaTweets(
        getPageData = { cursor -> getTwitterUserMediaAsync(userId, screenName, cursor) },
        onNewItems = onNewItems,
        onError = onError
    )

    private suspend fun <T> fetchTwitterPage(
        apiUrl: String,
        params: Map<String, String>,
        addHeaders: (Request.Builder) -> Request.Builder,
        extractData: (RootDto, String) -> T
    ): NetworkResult<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .get()
                .url(buildTwitterUrl(apiUrl, params))
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
    private fun buildTwitterUrl(baseUrl: String, params: Map<String, String>): String {
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

fun extractMissEvanSoundId(url: String): String? {
    // 匹配可能的 URL 模式
    val patterns = listOf(
        """missevan\.com/sound/(\d+)""".toRegex(),         // 普通链接
        """missevan\.com/sound/player\?id=(\d+)""".toRegex() // 播放器链接
    )

    patterns.forEach { pattern ->
        pattern.find(url)?.groupValues?.getOrNull(1)?.let { id ->
            return id
        }
    }
    return null
}

private val DRAMA_PATTERN = """missevan\.com/mdrama/(\d+)""".toRegex()

fun extractDramaId(url: String): String? {
    return DRAMA_PATTERN.find(url)?.groupValues?.getOrNull(1)
}