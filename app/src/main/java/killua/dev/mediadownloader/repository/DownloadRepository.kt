package killua.dev.mediadownloader.repository

import android.net.Uri
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.MostDownloadedUser
import killua.dev.mediadownloader.db.DownloadDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {

    // ✅ 监听所有下载项（UI 自动更新）
    fun observeAllDownloads(): Flow<List<Download>> = downloadDao.observeAllDownloads()

    // ✅ 通过 UUID 获取下载项
    suspend fun getById(uuid: String): Download? = downloadDao.getById(uuid)

    // ✅ 插入下载项
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download) = downloadDao.insert(download)

    // ✅ 删除下载项
    suspend fun delete(download: Download) = downloadDao.delete(download)

    // ✅ 通过 Twitter 用户 ID 查询下载记录
    suspend fun getByUserID(userID: String): List<Download> = downloadDao.getByUserID(userID)

    // ✅ 获取状态为 "DOWNLOADING" 的任务
    suspend fun getDownloadingItems(): List<Download> = downloadDao.getDownloading()

    suspend fun getMostDownloadedUser(): MostDownloadedUser? = downloadDao.getMostDownloadedUser()

    // ✅ 获取所有 Pending 状态的下载项（等待下载）
    suspend fun getPendingDownloads(): List<Download> = downloadDao.getByStatus(DownloadStatus.PENDING)

    suspend fun getByStatus(status: DownloadStatus): List<Download> = downloadDao.getByStatus(status)

    suspend fun deleteById(uuid: String) = downloadDao.deleteById(uuid)

    suspend fun getActiveDownloads() = downloadDao.getActiveDownloads()

    // ✅ 更新下载状态

    suspend fun updateStatus(uuid: String, status: DownloadStatus) {
        downloadDao.updateStatus(uuid, status)
    }

    suspend fun updateDownloadingStatus(uuid: String) = downloadDao.updateDownloadingStatus(uuid = uuid)
    suspend fun updateCompletedStatus(uuid: String) = downloadDao.updateCompletedStatus(uuid = uuid)
    suspend fun updateFailedStatus(uuid: String) = downloadDao.updateFailedStatus(uuid = uuid)

    // ✅ 下载完成时更新
    suspend fun updateCompleted(
        uuid: String,
        fileUri: Uri,
        fileSize: Long
    ) {
        downloadDao.updateCompleted(
            uuid = uuid,
            status = DownloadStatus.COMPLETED,
            fileUri = fileUri,
            fileSize = fileSize,
            completedAt = System.currentTimeMillis()
        )
    }

    // ✅ 记录下载失败信息
    suspend fun updateError(uuid: String, errorMessage: String?) {
        downloadDao.updateError(uuid, status = DownloadStatus.FAILED, errorMessage = errorMessage)
    }


}