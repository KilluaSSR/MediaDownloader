package killua.dev.base.Model

import android.net.Uri
import android.provider.MediaStore

sealed class MediaStoreConfig(
    val mimeType: String,
    val collection: Uri,
    val relativePath: (String) -> String
) {
    object Video : MediaStoreConfig(
        mimeType = "video/mp4",
        collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        relativePath = { "Movies/$it" }
    )

    object Photo : MediaStoreConfig(
        mimeType = "image/jpeg",
        collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
        relativePath = { "Pictures/$it" }
    )
}