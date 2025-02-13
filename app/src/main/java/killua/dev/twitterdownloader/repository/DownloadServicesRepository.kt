package killua.dev.twitterdownloader.repository

import db.Download
import killua.dev.twitterdownloader.api.Kuaikan.KuaikanService
import killua.dev.twitterdownloader.api.Lofter.LofterService
import killua.dev.twitterdownloader.api.Pixiv.PixivService
import killua.dev.twitterdownloader.api.Twitter.TwitterDownloadAPI
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
    suspend fun getKuaikanMedia(url: String) = kuaikanService.getSingleChapter(url)
    suspend fun insert(download: Download) = downloadRepository.insert(download)
}