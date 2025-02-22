package killua.dev.mediadownloader.features

import android.content.Context
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.Model.MediaType
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.Kuaikan.Chapter
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.MissEvan.MissEvanService
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDownloadDrama
import killua.dev.mediadownloader.api.MissEvan.Model.MissEvanDramaResult
import killua.dev.mediadownloader.api.Pixiv.Model.NovelInfo
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.Model.TwitterUser
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.datastore.readLofterEndTime
import killua.dev.mediadownloader.datastore.readLofterStartTime
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.FileUtils
import killua.dev.mediadownloader.utils.KUAIKAN_ENTIRE_NOTIFICATION_ID
import killua.dev.mediadownloader.utils.LOFTER_GET_BY_TAGS_ID
import killua.dev.mediadownloader.utils.MISSEVAN_ENTIRE_DRAMA_ID
import killua.dev.mediadownloader.utils.MediaFileNameStrategy
import killua.dev.mediadownloader.utils.PIXIV_ENTIRE_NOTIFICATION_ID
import killua.dev.mediadownloader.utils.ShowNotification
import killua.dev.mediadownloader.utils.StringUtils.formatUnicodeToReadable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class AdvancedFeaturesManager @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val kuaikanService: KuaikanService,
    private val lofterService: LofterService,
    private val pixelService: PixivService,
    private val missEvanService: MissEvanService,
    private val notification: ShowNotification,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
    private val tagsRepository: LofterTagsRepository,
    private val preChecks: DownloadPreChecks,
    private val fileUtils: FileUtils,
    @ApplicationScope private val context: Context
) {
    fun isTwitterLoggedIn() = preChecks.checkTwitterLoggedIn()

    fun isLofterLoggedIn() = preChecks.checkLofterLoggedIn()

    suspend fun readLofterTags() = tagsRepository.observeAllDownloads().first()?.tags

    suspend fun readStartDateAndEndDate() = Pair(context.readLofterStartTime().first(),context.readLofterEndTime().first())

    fun cancelKuaikanProgressNotification() = notification.cancelSpecificNotification(KUAIKAN_ENTIRE_NOTIFICATION_ID)
    fun cancelPixivProgressNotification() = notification.cancelSpecificNotification(PIXIV_ENTIRE_NOTIFICATION_ID)
    fun cancelMissEvanProgressNotification() = notification.cancelSpecificNotification(MISSEVAN_ENTIRE_DRAMA_ID)
    suspend fun handleTwitterBookmarks(): Result<Unit> = runCatching {
        twitterDownloadAPI.getBookmarksAllTweets(
            onNewItems = { bookmarks ->
                bookmarks.forEach { bookmark ->
                    processTwitterMedia(bookmark.videoUrls, bookmark.user, bookmark.tweetId, MediaType.VIDEO)
                    processTwitterMedia(bookmark.photoUrls, bookmark.user, bookmark.tweetId, MediaType.PHOTO)
                }
            },
            onError = { throw Exception(it) }
        )
    }

    suspend fun handleTwitterLikes(): Result<Unit> = runCatching {
        twitterDownloadAPI.getLikesAllTweets(
            onNewItems = { tweets ->
                tweets.forEach { tweet ->
                    processTwitterMedia(tweet.videoUrls, tweet.user, tweet.tweetId, MediaType.VIDEO)
                    processTwitterMedia(tweet.photoUrls, tweet.user, tweet.tweetId, MediaType.PHOTO)
                }
            },
            onError = { throw Exception(it) }
        )
    }

    suspend fun getUserMediaByUserId(userId: String, screenName: String): Result<Unit> = runCatching {
        twitterDownloadAPI.getUserMediaByUserId(
            userId = userId,
            screenName = screenName,
            onNewItems = { tweets ->
                tweets.forEach { tweet ->
                    processTwitterMedia(tweet.videoUrls, tweet.user, tweet.tweetId, MediaType.VIDEO)
                    processTwitterMedia(tweet.photoUrls, tweet.user, tweet.tweetId, MediaType.PHOTO)
                }
            },
            onError = { throw Exception(it) }
        )
    }

    suspend fun getLofterPicsByAuthorTags(url: String){
        var link = url
        if(url.last() != '/'){
            link += "/"
        }
        val tags = tagsRepository.getAllTags()
        notification.showStartGettingLofterImages()
        val blogInfo = lofterService.getByAuthorTags(link, tags)
        notification.cancelSpecificNotification(LOFTER_GET_BY_TAGS_ID)
        val authorID = blogInfo.authorId
        val authorName = blogInfo.authorName
        val authorDomain = blogInfo.authorDomain
        blogInfo.images.forEach {
            delay(Random.nextLong(10, 300))
            createDownloadTask(
                url = it.url,
                userId = authorID,
                screenName = authorDomain,
                platform = AvailablePlatforms.Lofter,
                name = authorName,
                uniqueID = it.blogUrl,
                mainLink = it.url,
                mediaType = MediaType.PHOTO
            )

            println(it.url.substringBefore("?"))
        }
    }

    suspend fun getKuaikanEntireComic(url: String): NetworkResult<List<Chapter>> = runCatching {
        when(val result = kuaikanService.getEntireComic(url)) {
            is NetworkResult.Error -> {
                NetworkResult.Error(
                    code = result.code,
                    message = result.message
                )
            }
            is NetworkResult.Success -> {
                val manga = result.data
                NetworkResult.Success(manga)
            }
        }
    }.getOrElse { e ->
        NetworkResult.Error(
            message = e.message ?: "Unknown error"
        )
    }

    suspend fun getPixivEntireNovel(url: String): NetworkResult<List<NovelInfo>> = runCatching {
        val id = url.split("series/")[1]
        when(val result = pixelService.getEntireNovel(id)) {
            is NetworkResult.Error -> {
                NetworkResult.Error(
                    code = result.code,
                    message = result.message
                )
            }
            is NetworkResult.Success -> {
                val novel = result.data
                NetworkResult.Success(novel)
            }
        }
    }.getOrElse { e ->
        NetworkResult.Error(
            message = e.message ?: "Unknown error"
        )
    }

    suspend fun getMissEvanEntireDrama(url: String): NetworkResult<MissEvanDramaResult> = runCatching {
        val id = url.split("mdrama/")[1]
        when(val result = missEvanService.getEntireDrama(id)) {
            is NetworkResult.Error -> {
                NetworkResult.Error(
                    code = result.code,
                    message = result.message
                )
            }
            is NetworkResult.Success -> {
                val drama = result.data
                NetworkResult.Success(drama)
            }
        }
    }.getOrElse { e ->
        NetworkResult.Error(
            message = e.message ?: "Unknown error"
        )
    }

    suspend fun downloadEntireKuaikanComic(mangaList: List<Chapter>) = runCatching {
        mangaList.forEach{
            delay(Random.nextLong(500, 7000))
            notification.updateGettingProgress(it.name)
            when(val mangaResult = kuaikanService.getSingleChapter("https://www.kuaikanmanhua.com/webs/comic-next/${it.id}")){
                is NetworkResult.Error -> return@forEach
                is NetworkResult.Success -> {
                    createDownloadTask(
                        url = mangaResult.data.urlList.joinToString(separator = ","),
                        userId = mangaResult.data.title,
                        screenName = mangaResult.data.title,
                        platform = AvailablePlatforms.Kuaikan,
                        name = mangaResult.data.chapter,
                        uniqueID = "https://www.kuaikanmanhua.com/webs/comic-next/${it.id}",
                        mainLink = "https://www.kuaikanmanhua.com/webs/comic-next/${it.id}",
                        mediaType = MediaType.PDF
                    )
                }
            }
        }
    }

    suspend fun downloadEntireMissEvanDrama(dramaList:  List<MissEvanDownloadDrama>) = runCatching {
        dramaList.forEach{
            delay(Random.nextLong(500, 3000))
            notification.updateGettingProgress(it.title)
            when(val result = missEvanService.getDrama(it.id)){
                is NetworkResult.Error -> return@forEach
                is NetworkResult.Success -> {
                    createDownloadTask(
                        url = result.data.soundurl,
                        userId = it.mainTitle,
                        screenName = it.mainTitle,
                        platform = AvailablePlatforms.MissEvan,
                        name = it.title,
                        uniqueID = "https://www.missevan.com/sound/player?id=${it.id}",
                        mainLink = result.data.soundurl,
                        mediaType = MediaType.M4A
                    )
                }
            }
        }
    }

    suspend fun downloadEntirePixivNovel(novelList: List<NovelInfo>) = runCatching {
        novelList.forEach{
            notification.updateGettingProgress(it.title, downloadId = PIXIV_ENTIRE_NOTIFICATION_ID, type = "novel")
            when(val result = pixelService.getNovel(it.id)) {
                is NetworkResult.Success -> {
                    val formattedContent = result.data.content.formatUnicodeToReadable()
                    val formattedTitle = result.data.title.formatUnicodeToReadable()
                    fileUtils.writeTextToFile(
                        mainFolder = it.seriesTitle,
                        text = formattedContent,
                        fileName = formattedTitle,
                        mediaType = MediaType.TXT,
                        platform = AvailablePlatforms.Pixiv
                    )
                }
                is NetworkResult.Error -> throw Exception("Pixiv request error")
            }
        }
    }

    private suspend fun processTwitterMedia(
        urls: List<String>,
        user: TwitterUser?,
        tweetId: String,
        mediaType: MediaType
    ) {
        urls.forEach { url ->
            createDownloadTask(url, user!!.id, user.screenName!!, AvailablePlatforms.Twitter, user.name!!, tweetId, "x.com/${user.screenName}/status/$tweetId", mediaType)
            delay(Random.nextLong(50, 150))
        }
    }

    private suspend fun createDownloadTask(
        url: String,
        userId: String?,
        screenName: String,
        platform: AvailablePlatforms,
        name: String,
        uniqueID: String,
        mainLink: String,
        mediaType: MediaType
    ) {
        val fileNameStrategy = MediaFileNameStrategy(mediaType)
        val fileName = when(platform) {
            AvailablePlatforms.Kuaikan -> fileNameStrategy.generateManga(title = screenName, chapter = name)
            else -> fileNameStrategy.generateMedia(screenName)
        }

        val download = Download(
            uuid = UUID.randomUUID().toString(),
            userId = userId,
            screenName = screenName,
            platform = platform,
            name = name,
            uniqueID = uniqueID,
            fileUri = null,
            link = mainLink,
            fileName = fileName,
            fileType = mediaType.name.lowercase(),
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            mimeType = mediaType.mimeType
        )

        downloadRepository.insert(download)
        downloadQueueManager.enqueue(
            DownloadTask(
                id = download.uuid,
                url = url,
                refererNecessary = mainLink,
                from = download.platform,
                fileName = fileName,
                screenName = screenName,
                type = mediaType
            )
        )
    }
}