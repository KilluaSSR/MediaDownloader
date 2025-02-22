package killua.dev.mediadownloader.download

import android.util.Log
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.Model.MediaType
import killua.dev.mediadownloader.Model.NetworkResult
import killua.dev.mediadownloader.repository.DownloadServicesRepository
import killua.dev.mediadownloader.utils.DownloadEventManager
import killua.dev.mediadownloader.utils.DownloadPreChecks
import killua.dev.mediadownloader.utils.FileUtils
import killua.dev.mediadownloader.utils.MediaFileNameStrategy
import killua.dev.mediadownloader.utils.StringUtils.formatUnicodeToReadable
import java.util.UUID
import javax.inject.Inject

class DownloadbyLink @Inject constructor(
    private val downloadRepository: DownloadServicesRepository,
    private val downloadQueueManager: DownloadQueueManager,
    private val downloadEventManager: DownloadEventManager,
    private val downloadPreChecks: DownloadPreChecks,
    private val fileUtils: FileUtils
) {
    val downloadCompletedFlow = downloadEventManager.downloadCompletedFlow

    suspend fun handlePlatformDownload(url: String, platform: AvailablePlatforms): Result<Unit> {
        return downloadPreChecks.canStartDownload().map {
            when(platform) {
                AvailablePlatforms.Twitter -> handleTwitterDownload(url)
                AvailablePlatforms.Lofter -> handleLofterDownload(url)
                AvailablePlatforms.Pixiv -> handlePixivDownload(url)
                AvailablePlatforms.Kuaikan -> handleKuaikanDownload(url)
                AvailablePlatforms.MissEvan ->  handleMissEvanDownload(url)
            }
        }
    }

    fun checkPlatformLogin(platform: AvailablePlatforms): Result<Unit> {
        return when(platform) {
            AvailablePlatforms.Twitter -> downloadPreChecks.checkTwitterLoggedIn()
            AvailablePlatforms.Lofter -> downloadPreChecks.checkLofterLoggedIn()
            AvailablePlatforms.Pixiv -> downloadPreChecks.checkPixivLoggedIn()
            else -> Result.success(Unit)
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
                    uniqueID = url,
                    mainLink = url,
                    mediaType = MediaType.PDF
                )
            }
            is NetworkResult.Error -> throw Exception(result.message)
        }
    }

    private suspend fun handlePixivDownload(url: String) {
        if(url.contains("artwork")){
            val id = try {
                url.split("artworks/")[1]
            } catch (e: Exception) {
                return
            }
            when(val result = downloadRepository.getPixivMedia(id)) {
                is NetworkResult.Success -> {
                    result.data.originalUrls.forEach { imageURL ->
                        createDownloadTask(
                            url = imageURL,
                            userId = result.data.userId,
                            screenName = result.data.userName,
                            platform = AvailablePlatforms.Pixiv,
                            name = result.data.title,
                            uniqueID = imageURL,
                            mainLink = url,
                            mediaType = MediaType.PHOTO
                        )
                    }
                }
                is NetworkResult.Error -> throw Exception("Pixiv request error")
            }
        }else if (url.contains("novel")){
            val id = try {
                url.split("show.php?id=")[1]
            } catch (e: Exception) {
                return
            }
            when(val result = downloadRepository.getPixivNovel(id)) {
                is NetworkResult.Success -> {
                    val formattedContent = result.data.content.formatUnicodeToReadable()
                    val formattedTitle = result.data.title.formatUnicodeToReadable()
                    fileUtils.writeTextToFile(
                        mainFolder = result.data?.seriesNavData?.title,
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
                        uniqueID = url,
                        mainLink = image.url,
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
                        uniqueID = tweetId,
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
                                uniqueID = tweetId,
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

    private suspend fun handleMissEvanDownload(url: String) {
        val id = url.split("id=")[1]
        when(val result = downloadRepository.getMissEvanDrama(id)) {
            is NetworkResult.Success -> {
                createDownloadTask(
                    url = result.data.soundurl,
                    userId = result.data.soundstr,
                    screenName = result.data.soundstr,
                    platform = AvailablePlatforms.MissEvan,
                    name = result.data.soundstr,
                    uniqueID = url,
                    mainLink = result.data.soundurl,
                    mediaType = MediaType.M4A
                )
            }
            is NetworkResult.Error -> throw Exception(result.message)
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
                from = download.platform,
                fileName = fileName,
                screenName = screenName,
                type = mediaType
            )
        )
    }
}