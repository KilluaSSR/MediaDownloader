package killua.dev.mediadownloader.db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import db.Download
import db.DownloadStatus
import killua.dev.mediadownloader.Model.MostDownloadedUser
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM Download ORDER BY created_at DESC")
    fun observeAllDownloads(): Flow<List<Download>>

    @Query("SELECT * FROM Download ORDER BY created_at DESC")
    suspend fun getAll(): List<Download>

    @Query("SELECT * FROM Download WHERE uuid = :uuid")
    suspend fun getById(uuid: String): Download?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg downloads: Download)

    @Delete
    suspend fun delete(download: Download)

    @Query("SELECT * FROM Download WHERE user_id = :userID")
    suspend fun getByUserID(userID: String): List<Download>

    @Query("SELECT * FROM Download WHERE status = :status")
    suspend fun getByStatus(status: DownloadStatus): List<Download>

    @Query("SELECT * FROM Download WHERE status = 'DOWNLOADING'")
    suspend fun getDownloading(): List<Download>

    @Query("SELECT * FROM Download WHERE file_type = :fileType")
    suspend fun getByFileType(fileType: String): List<Download>

    @Query("""
        UPDATE Download 
        SET status = :status
        WHERE uuid = :uuid
    """)
    suspend fun updateStatus(uuid: String, status: DownloadStatus)


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


    @Query("""
    SELECT 
        user_id AS userID,
        screen_name AS screenName,
        name AS name,
        COUNT(*) AS totalDownloads,
        platform AS platforms
    FROM Download
    WHERE status = :status 
        AND user_id IS NOT NULL 
        AND user_id != ''
        AND screen_name IS NOT NULL 
        AND screen_name != ''
        AND name IS NOT NULL 
        AND name != ''
        AND platform IN ('Twitter', 'Lofter', 'PIXIV')
    GROUP BY user_id, platform
    ORDER BY totalDownloads DESC
    LIMIT 1
""") suspend fun getMostDownloadedUser(status: DownloadStatus = DownloadStatus.COMPLETED): MostDownloadedUser?

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