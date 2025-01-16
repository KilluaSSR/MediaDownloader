package repository

import api.Model.TwitterUser
import db.Download
import db.DownloadDao
import db.DownloadStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    suspend fun createDownloadEntry(
        videoUrl: String,
        twitterUser: TwitterUser
    ): String {
        val uuid = UUID.randomUUID().toString()
        val download = Download(
            uuid = uuid,
            twitterUserId = twitterUser.id,
            twitterScreenName = twitterUser.screenName,
            twitterName = twitterUser.name,
            fileUri = null, // 暂时为null，等待下载完成后更新
            link = videoUrl,
            fileName = "", // TODO: 从URL生成文件名
            fileType = "video/mp4",
            fileSize = 0L, // TODO: 获取实际文件大小
            status = DownloadStatus.PENDING
        )

        downloadDao.insert(download)
        return uuid
    }

    suspend fun getAllDownloads() = downloadDao.getAllByDateDesc()

    suspend fun getDownloadingItems() = downloadDao.getDownloading()

    suspend fun updateDownloadProgress(uuid: String, progress: Int) {
        downloadDao.updateProgress(
            uuid = uuid,
            status = DownloadStatus.DOWNLOADING,
            progress = progress
        )
    }
}