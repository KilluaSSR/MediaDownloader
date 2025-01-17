package db

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlin.uuid.ExperimentalUuidApi

@Dao
interface DownloadDao {
    
    @Query("SELECT * FROM Download")
    suspend fun getAll(): List<Download>

    @OptIn(ExperimentalUuidApi::class)
    @Query("SELECT * FROM Download WHERE uuid = :uuid")
    suspend fun getById(uuid: String): Download?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: Download)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg download: Download)

    @Delete
    suspend fun delete(download: Download)

    @Query("SELECT * FROM Download WHERE twitter_user_id = :userID")
    suspend fun getByUserID(userID: String): Download?

    @Query("SELECT * FROM Download WHERE status = :status")
    suspend fun getByStatus(status: DownloadStatus): List<Download>

    @Query("SELECT * FROM Download WHERE status = 'DOWNLOADING'")
    suspend fun getDownloading(): List<Download>


    @Query("SELECT * FROM Download ORDER BY created_at DESC")
    suspend fun getAllByDateDesc(): List<Download>


    @Query("SELECT * FROM Download WHERE file_type = :fileType")
    suspend fun getByFileType(fileType: String): List<Download>


    @OptIn(ExperimentalUuidApi::class)
    @Query("UPDATE Download SET status = :status, progress = :progress WHERE uuid = :uuid")
    suspend fun updateProgress(uuid: String, status: DownloadStatus, progress: Int)

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
    @OptIn(ExperimentalUuidApi::class)
    @Query("UPDATE Download SET status = :status, error_message = :errorMessage WHERE uuid = :uuid")
    suspend fun updateError(uuid: String, status: DownloadStatus = DownloadStatus.FAILED, errorMessage: String?)
}