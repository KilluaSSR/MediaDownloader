package killua.dev.twitterdownloader.features

import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.utils.MediaFileNameStrategy
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.api.Kuaikan.KuaikanService
import killua.dev.twitterdownloader.api.Lofter.LofterService
import killua.dev.twitterdownloader.api.Twitter.Model.TwitterUser
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
import killua.dev.twitterdownloader.download.DownloadQueueManager
import killua.dev.twitterdownloader.repository.DownloadRepository
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import kotlin.random.Random

class AdvancedFeaturesManager @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val kuaikanService: KuaikanService,
    private val lofterService: LofterService,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadRepository: DownloadRepository,
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

    suspend fun getWholeManga(url: String): Result<Unit> = runCatching {
        when(val result = kuaikanService.getWholeMange(url)){
            is NetworkResult.Error -> {}
            is NetworkResult.Success -> {
                val manga = result.data
                manga.forEach{
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
                                mainLink = url,
                                mediaType = MediaType.PDF
                            )
                        }
                    }
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