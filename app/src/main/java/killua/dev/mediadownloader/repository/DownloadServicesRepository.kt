package killua.dev.mediadownloader.repository

import db.Download
import killua.dev.mediadownloader.api.PlatformService
import javax.inject.Inject

class DownloadServicesRepository @Inject constructor(
    private val platformService: PlatformService,
    private val downloadRepository: DownloadRepository
) {
    suspend fun getTwitterMedia(tweetId: String) = platformService.getTwitterSingleMediaDetailAsync(tweetId)
    fun getLofterMedia(url: String) = platformService.getLofterBlogImages(url)
    suspend fun getPixivMedia(id: String) = platformService.getSinglePixivBlogImage(id)
    suspend fun getMissEvanDrama(id: String) = platformService.getMissEvanDrama(id)
    suspend fun getPixivNovel(id: String) = platformService.getPixivNovel(id)
    suspend fun getKuaikanMedia(url: String) = platformService.getSingleChapter(url)
    suspend fun insert(download: Download) = downloadRepository.insert(download)
}