package killua.dev.mediadownloader.features

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.Model.MediaType
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.api.Kuaikan.Chapter
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.Twitter.Model.TwitterUser
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.mediadownloader.datastore.readLofterEndTime
import killua.dev.mediadownloader.datastore.readLofterLoginAuth
import killua.dev.mediadownloader.datastore.readLofterLoginKey
import killua.dev.mediadownloader.datastore.readLofterStartTime
import killua.dev.mediadownloader.db.LofterTagsRepository
import killua.dev.mediadownloader.di.ApplicationScope
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.repository.DownloadRepository
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.KUAIKAN_ENTIRE_NOTIFICATION_ID
import killua.dev.mediadownloader.utils.LOFTER_GET_BY_TAGS_ID
import killua.dev.mediadownloader.utils.MediaFileNameStrategy
import killua.dev.mediadownloader.utils.ShowNotification
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class AdvancedFeaturesManager @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val kuaikanService: KuaikanService,
    private val lofterService: LofterService,
    private val notification: ShowNotification,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
    private val tagsRepository: LofterTagsRepository,
    private val preChecks: DownloadPreChecks,
    @ApplicationScope private val context: Context
) {
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

    fun isTwitterLoggedIn() = preChecks.checkTwitterLoggedIn()

    suspend fun readLofterTags() = tagsRepository.observeAllDownloads().first()?.tags

    suspend fun readStartDateAndEndDate() = Pair(context.readLofterStartTime().first(),context.readLofterEndTime().first())

    suspend fun readLofterCredits() = Pair(context.readLofterLoginKey().first(),context.readLofterLoginAuth().first())

    fun cancelKuaikanProgressNotification() = notification.cancelSpecificNotification(KUAIKAN_ENTIRE_NOTIFICATION_ID)

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

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
                tweetID = it.url,
                mainLink = it.blogUrl,
                mediaType = MediaType.PHOTO
            )

            println(it.url.substringBefore("?"))
        }
    }

    suspend fun getWholeManga(url: String): NetworkResult<List<Chapter>> = runCatching {
        when(val result = kuaikanService.getWholeComic(url)) {
            is NetworkResult.Error -> {
                println(result.code)
                println(result.message)
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

    suspend fun downloadEntireManga(mangaList: List<Chapter>) = runCatching {
        mangaList.forEach{
            delay(Random.nextLong(500, 7000))
            notification.updateGettingComicProgress(it.name)
            when(val mangaResult = kuaikanService.getSingleChapter("https://www.kuaikanmanhua.com/webs/comic-next/${it.id}")){
                is NetworkResult.Error -> return@forEach
                is NetworkResult.Success -> {
                    createDownloadTask(
                        url = mangaResult.data.urlList.joinToString(separator = ","),
                        userId = mangaResult.data.title,
                        screenName = mangaResult.data.title,
                        platform = AvailablePlatforms.Kuaikan,
                        name = mangaResult.data.chapter,
                        tweetID = mangaResult.data.title,
                        mainLink = "https://www.kuaikanmanhua.com/webs/comic-next/${it.id}",
                        mediaType = MediaType.PDF
                    )
                }
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
        tweetID: String,
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
            type = platform,
            name = name,
            tweetID = tweetID,
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
                from = download.type,
                fileName = fileName,
                screenName = screenName,
                type = mediaType
            )
        )
    }
}