package killua.dev.twitterdownloader.download

import db.Download
import db.DownloadStatus
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.Model.DownloadTask
import killua.dev.base.Model.MediaType
import killua.dev.base.utils.DownloadEventManager
import killua.dev.base.utils.DownloadPreChecks
import killua.dev.base.utils.MediaFileNameStrategy
import killua.dev.twitterdownloader.Model.NetworkResult
import killua.dev.twitterdownloader.repository.DownloadServicesRepository
import java.util.UUID
import javax.inject.Inject
class DownloadbyLink @Inject constructor(
private val downloadRepository: DownloadServicesRepository,
private val downloadQueueManager: DownloadQueueManager,
private val downloadEventManager: DownloadEventManager,
private val downloadPreChecks: DownloadPreChecks
) {
    val downloadCompletedFlow = downloadEventManager.downloadCompletedFlow

    suspend fun handlePlatformDownload(url: String, platform: AvailablePlatforms): Result<Unit> {
        return downloadPreChecks.canStartDownload().map {
            when(platform) {
                AvailablePlatforms.Twitter -> handleTwitterDownload(url)
                AvailablePlatforms.Lofter -> handleLofterDownload(url)
                AvailablePlatforms.Pixiv -> handlePixivDownload(url)
                AvailablePlatforms.Kuaikan -> handleKuaikanDownload(url)
            }
        }
    }

    fun checkPlatformLogin(platform: AvailablePlatforms): Result<Unit> {
        return when(platform) {
            AvailablePlatforms.Twitter -> downloadPreChecks.checkTwitterLoggedIn()
            AvailablePlatforms.Lofter -> downloadPreChecks.checkLofterLoggedIn()
            AvailablePlatforms.Pixiv -> downloadPreChecks.checkPixivLoggedIn()
            AvailablePlatforms.Kuaikan -> Result.success(Unit)
        }
    }

    private suspend fun handleKuaikanDownload(url: String) {
        when(val result = downloadRepository.getKuaikanMedia(url)) {
            is NetworkResult.Success -> {
                createDownloadTask(
                    url = result.data.urlList.joinToString(separator = ","),
                    userId = result.data.title,
                    screenName = result.data.title,
                    platform = AvailablePlatforms.Kuaikan,
                    name = result.data.chapter,
                    tweetID = result.data.title,
                    mainLink = url,
                    mediaType = MediaType.PDF
                )
            }
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    private suspend fun handlePixivDownload(url: String) {
        when(val result = downloadRepository.getPixivMedia(url)) {
            is NetworkResult.Success -> {
                result.data.originalUrls.forEach { imageURL ->
                    createDownloadTask(
                        url = imageURL,
                        userId = result.data.userId,
                        screenName = result.data.userName,
                        platform = AvailablePlatforms.Pixiv,
                        name = result.data.title,
                        tweetID = imageURL,
                        mainLink = url,
                        mediaType = MediaType.PHOTO
                    )
                }
            }
            is NetworkResult.Error -> throw Exception("Pixiv request error")
        }
    }

    private suspend fun handleLofterDownload(url: String) {
        when (val result = downloadRepository.getLofterMedia(url)) {
            is NetworkResult.Success -> {
                val data = result.data
                data.images.forEach { image ->
                    createDownloadTask(
                        url = image.url,
                        userId = data.authorId,
                        screenName = data.authorDomain,
                        platform = AvailablePlatforms.Lofter,
                        name = data.authorName,
                        tweetID = image.url,
                        mainLink = url,
                        mediaType = MediaType.PHOTO
                    )
                }
            }
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    private suspend fun handleTwitterDownload(url: String) {
        val tweetId = url.split("?")[0].split("/").last()
        when (val result = downloadRepository.getTwitterMedia(tweetId)) {
            is NetworkResult.Success -> {
                val user = result.data.user
                result.data.videoUrls.forEach { videoUrl ->
                    createDownloadTask(
                        url = videoUrl,
                        userId = user?.id,
                        screenName = user?.screenName ?: "",
                        platform = AvailablePlatforms.Twitter,
                        name = user?.name ?: "",
                        tweetID = tweetId,
                        mainLink = videoUrl,
                        mediaType = MediaType.VIDEO
                    )
                }

                if(result.data.photoUrls.isNotEmpty()) {
                    downloadPreChecks.checkPhotosDownload().onSuccess {
                        result.data.photoUrls.forEach { photoUrl ->
                            createDownloadTask(
                                url = photoUrl,
                                userId = user?.id,
                                screenName = user?.screenName ?: "",
                                platform = AvailablePlatforms.Twitter,
                                name = user?.name ?: "",
                                tweetID = tweetId,
                                mainLink = photoUrl,
                                mediaType = MediaType.PHOTO
                            )
                        }
                    }
                }
            }
            is NetworkResult.Error -> throw Exception("Twitter request error")
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