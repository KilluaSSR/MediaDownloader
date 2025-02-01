package db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import killua.dev.twitterdownloader.Model.MostDownloadedUser
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@Dao
interface DownloadDao {

    // ✅ 监听所有下载项（Flow 方式，让 UI 自动更新）
    @Query("SELECT * FROM Download ORDER BY created_at DESC")
    fun observeAllDownloads(): Flow<List<Download>>

    // ✅ 获取所有下载项（一次性获取）
    @Query("SELECT * FROM Download ORDER BY created_at DESC")
    suspend fun getAll(): List<Download>

    // ✅ 通过 UUID 查询下载项
    @Query("SELECT * FROM Download WHERE uuid = :uuid")
    suspend fun getById(uuid: String): Download?

    // ✅ 插入单个下载项，若冲突则替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download)

    // ✅ 批量插入，若冲突则替换
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg downloads: Download)

    // ✅ 删除指定下载项
    @Delete
    suspend fun delete(download: Download)

    // ✅ 通过 Twitter 用户 ID 查询下载记录
    @Query("SELECT * FROM Download WHERE twitter_user_id = :userID")
    suspend fun getByUserID(userID: String): List<Download>

    // ✅ 根据下载状态查询
    @Query("SELECT * FROM Download WHERE status = :status")
    suspend fun getByStatus(status: DownloadStatus): List<Download>

    // ✅ 查询正在下载的任务
    @Query("SELECT * FROM Download WHERE status = 'DOWNLOADING'")
    suspend fun getDownloading(): List<Download>

    // ✅ 按文件类型查询
    @Query("SELECT * FROM Download WHERE file_type = :fileType")
    suspend fun getByFileType(fileType: String): List<Download>

    // ✅ 更新下载进度
    @Query("""
        UPDATE Download 
        SET status = :status, progress = :progress 
        WHERE uuid = :uuid
    """)
    suspend fun updateProgress(uuid: String, status: DownloadStatus, progress: Int)


    @Query("""
        UPDATE Download 
        SET status = :status
        WHERE uuid = :uuid AND status != 'DOWNLOADING'
    """)
    suspend fun updateDownloadingStatus(uuid: String, status: DownloadStatus = DownloadStatus.DOWNLOADING)

    @Query("""
        UPDATE Download 
        SET status = :status
        WHERE uuid = :uuid AND status != 'COMPLETED'
    """)
    suspend fun updateCompletedStatus(uuid: String, status: DownloadStatus = DownloadStatus.COMPLETED)

    @Query("""
        UPDATE Download 
        SET status = :status
        WHERE uuid = :uuid AND status != 'FAILED'
    """)
    suspend fun updateFailedStatus(uuid: String, status: DownloadStatus = DownloadStatus.FAILED)

    // ✅ 下载完成时更新
    @Query("""
        UPDATE Download 
        SET status = :status, 
            file_uri = :fileUri, 
            file_size = :fileSize, 
            completed_at = :completedAt 
        WHERE uuid = :uuid
    """)
    suspend fun updateCompleted(
        uuid: String,
        status: DownloadStatus,
        fileUri: Uri,
        fileSize: Long,
        completedAt: Long
    )

    @Query("SELECT * FROM Download WHERE status IN ('PENDING', 'DOWNLOADING', 'FAILED')")
    suspend fun getActiveDownloads(): List<Download>

    @Query("DELETE FROM Download WHERE uuid = :uuid")
    suspend fun deleteById(uuid: String)

    // ✅ 获取下载次数最多的 Twitter 用户
    @Query("""
        SELECT 
            twitter_user_id AS twitterUserId,
            twitter_screen_name AS twitterScreenName,
            twitter_name AS twitterName,
            COUNT(*) AS totalDownloads
        FROM Download
        WHERE status = :status
        GROUP BY twitter_user_id
        ORDER BY totalDownloads DESC
        LIMIT 1
    """)
    suspend fun getMostDownloadedUser(status: DownloadStatus = DownloadStatus.COMPLETED): MostDownloadedUser?

    // ✅ 记录下载失败信息
    @Query("""
        UPDATE Download 
        SET status = :status, error_message = :errorMessage 
        WHERE uuid = :uuid
    """)
    suspend fun updateError(
        uuid: String,
        status: DownloadStatus = DownloadStatus.FAILED,
        errorMessage: String?
    )
}