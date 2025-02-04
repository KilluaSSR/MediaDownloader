package killua.dev.base.Model

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
    PHOTO(
        extension = "jpg",
        mimeType = "image/jpeg",
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        baseFolder = "Pictures"
    );

    fun buildPath(platform: AvailablePlatforms): String {
        val platformFolder = when (platform) {
            AvailablePlatforms.Twitter -> "TwitterDownloader"
            AvailablePlatforms.Lofter -> "LofterDownloader"
        }
        return "$baseFolder/$platformFolder"
    }

    companion object {
        fun fromString(value: String): MediaType = when(value.lowercase()) {
            "video" -> VIDEO
            "photo" -> PHOTO
            else -> throw IllegalArgumentException("Unknown media type: $value")
        }
    }
}