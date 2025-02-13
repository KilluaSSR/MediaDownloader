package killua.dev.twitterdownloader.features

import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.utils.MediaFileNameStrategy
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

    private suspend fun processTwitterMedia(
        urls: List<String>,
        user: TwitterUser?,
        tweetId: String,
        mediaType: MediaType
    ) {
        urls.forEach { url ->
            createAndStartDownload(url, user, tweetId, mediaType)
            delay(Random.nextLong(50, 150))
        }
    }

    private suspend fun createAndStartDownload(
        url: String,
        user: TwitterUser?,
        tweetId: String,
        mediaType: MediaType
    ) {
        val uuid = UUID.randomUUID().toString()
        val fileNameStrategy = MediaFileNameStrategy(mediaType)
        val fileName = fileNameStrategy.generateMedia(user?.screenName)

        val download = Download(
            uuid = uuid,
            userId = user?.id,
            screenName = user?.screenName,
            type = AvailablePlatforms.Twitter,
            name = user?.name,
            tweetID = tweetId,
            fileUri = null,
            link = url,
            fileName = fileName,
            fileType = mediaType.name.lowercase(),
            fileSize = 0L,
            status = DownloadStatus.PENDING,
            mimeType = mediaType.mimeType
        )

        downloadRepository.insert(download)
        downloadQueueManager.enqueue(
            DownloadTask(
                id = uuid,
                url = url,
                fileName = fileName,
                screenName = user?.screenName ?: "",
                type = mediaType
            )
        )
    }
}