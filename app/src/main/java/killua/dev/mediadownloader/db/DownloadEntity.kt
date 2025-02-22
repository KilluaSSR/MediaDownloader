package db

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import killua.dev.mediadownloader.Model.AvailablePlatforms
import kotlin.uuid.ExperimentalUuidApi

@Entity
data class Download @OptIn(ExperimentalUuidApi::class) constructor(
    @PrimaryKey val uuid: String,

    @ColumnInfo(name = "user_id") val userId: String? = null,
    @ColumnInfo(name = "screen_name") val screenName: String? = null,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "platform") val platform: AvailablePlatforms = AvailablePlatforms.Twitter,

    @ColumnInfo(name = "file_uri") val fileUri: Uri?,
    @ColumnInfo(name = "link") val link: String?,

    @ColumnInfo(name = "unique_id") val uniqueID: String?,

    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "file_type") val fileType: String,
    @ColumnInfo(name = "file_size") val fileSize: Long,

    @ColumnInfo(name = "range_header") val rangeHeader: String? = null,

    @ColumnInfo(name = "status") val status: DownloadStatus = DownloadStatus.PENDING,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,

    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,

    @ColumnInfo(name = "mime_type") val mimeType: String? = null,
)

class UriTypeConverter {
    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }
}