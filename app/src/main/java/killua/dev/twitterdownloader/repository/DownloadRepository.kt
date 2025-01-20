package killua.dev.twitterdownloader.repository

import android.net.Uri
import db.Download
import db.DownloadDao
import db.DownloadStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    suspend fun insert(download: Download) = downloadDao.insert(download)
    suspend fun getById(uuid: String) = downloadDao.getById(uuid)
    suspend fun delete(download: Download) = downloadDao.delete(download)
    suspend fun getByUserID(userID: String) = downloadDao.getByUserID(userID)
    suspend fun updateCompleted(
        uuid: String,
        fileUri: Uri,
        fileSize: Long
    ) = downloadDao.updateCompleted(
        uuid = uuid,
        status = DownloadStatus.COMPLETED,
        fileUri = fileUri,
        fileSize = fileSize,
        completedAt = System.currentTimeMillis()
    )

    suspend fun getAllDownloads() = downloadDao.getAllByDateDesc()

    suspend fun getDownloadingItems() = downloadDao.getDownloading()

    suspend fun updateDownloadProgress(uuid: String, progress: Int) {
        downloadDao.updateProgress(
            uuid = uuid,
            status = DownloadStatus.DOWNLOADING,
            progress = progress
        )
    }

    suspend fun updateError(
        uuid: String,
        status: DownloadStatus = DownloadStatus.FAILED,
        errorMessage: String?
    ) = downloadDao.updateError(uuid, status, errorMessage)
}