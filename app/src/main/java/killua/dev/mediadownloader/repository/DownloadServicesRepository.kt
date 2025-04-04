package killua.dev.mediadownloader.repository

import db.Download
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.MissEvan.MissEvanService
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import javax.inject.Inject

class DownloadServicesRepository @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val lofterService: LofterService,
    private val pixivService: PixivService,
    private val missEvanService: MissEvanService,
    private val kuaikanService: KuaikanService,
    private val downloadRepository: DownloadRepository
) {
    suspend fun getTwitterMedia(tweetId: String) = twitterDownloadAPI.getTwitterSingleMediaDetailAsync(tweetId)
    fun getLofterMedia(url: String) = lofterService.getBlogImages(url)
    suspend fun getPixivMedia(id: String) = pixivService.getSingleBlogImage(id)
    suspend fun getMissEvanDrama(id: String) = missEvanService.getDrama(id)
    suspend fun getPixivNovel(id: String) = pixivService.getNovel(id)
    suspend fun getKuaikanMedia(url: String) = kuaikanService.getSingleChapter(url)
    suspend fun insert(download: Download) = downloadRepository.insert(download)
}