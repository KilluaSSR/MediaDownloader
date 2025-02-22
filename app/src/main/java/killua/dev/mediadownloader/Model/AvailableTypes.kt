package killua.dev.mediadownloader.Model

import android.net.Uri
import android.provider.MediaStore


enum class MediaType(
    val extension: String,
    val mimeType: String,
    val collection: Uri,
    val baseFolder: String
) {
    VIDEO(
        extension = "mp4",
        mimeType = "video/mp4",
        collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Movies"
    ),
    M4A(
        extension = "m4a",
        mimeType = "audio/mp4",
        collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Download"
    ),
    PHOTO(
        extension = "jpg",
        mimeType = "image/jpeg",
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Pictures"
    ),
    GIF(
        extension = "gif",
        mimeType = "image/gif",
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Pictures"
    ),
    PDF(
        extension = "pdf",
        mimeType = "application/pdf",
        collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Download"
    ),
    TXT(
        extension = "txt",
        mimeType = "text/plain",
        collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Download"
    );

    fun buildPath(platform: AvailablePlatforms): String {
        val platformFolder = when (platform) {
            AvailablePlatforms.Twitter -> "TwitterDownloader"
            AvailablePlatforms.Lofter -> "LofterDownloader"
            AvailablePlatforms.Pixiv -> "PixivDownloader"
            AvailablePlatforms.Kuaikan -> "Manga/KuaikanManga"
            AvailablePlatforms.MissEvan -> "MissEvan"
        }
        return "$baseFolder/$platformFolder"
    }

    companion object {
        fun fromString(value: String): MediaType = when(value.lowercase()) {
            "video" -> VIDEO
            "photo" -> PHOTO
            "gif" -> GIF
            "pdf" -> PDF
            "txt" -> TXT
            "m4a" -> M4A
            else -> throw IllegalArgumentException("Unknown media type: $value")
        }
    }
}