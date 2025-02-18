package killua.dev.mediadownloader.repository

import db.Download
import killua.dev.mediadownloader.api.Kuaikan.KuaikanService
import killua.dev.mediadownloader.api.Lofter.LofterService
import killua.dev.mediadownloader.api.Pixiv.PixivService
import killua.dev.mediadownloader.api.Twitter.TwitterDownloadAPI
import javax.inject.Inject

class DownloadServicesRepository @Inject constructor(
    private val twitterDownloadAPI: TwitterDownloadAPI,
    private val lofterService: LofterService,
    private val pixivService: PixivService,
    private val kuaikanService: KuaikanService,
    private val downloadRepository: DownloadRepository
) {
    suspend fun getTwitterMedia(tweetId: String) = twitterDownloadAPI.getTwitterSingleMediaDetailAsync(tweetId)
    fun getLofterMedia(url: String) = lofterService.getBlogImages(url)
    suspend fun getPixivMedia(url: String) = pixivService.getSingleBlogImage(url)
    suspend fun getPixivNovel(url: String) = pixivService.getNovel(url)
    suspend fun getKuaikanMedia(url: String) = kuaikanService.getSingleChapter(url)
    suspend fun insert(download: Download) = downloadRepository.insert(download)
}