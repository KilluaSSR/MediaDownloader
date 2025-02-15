package killua.dev.mediadownloader.api.Pixiv.Model

import kotlinx.serialization.Serializable

@Serializable
data class PixivImageInfo(
    val userName: String,
    val userId: String,
    val title: String,
    val illustId: String,
    val originalUrls: List<String>
)

@Serializable
data class PixivPicturePageResponse(
    val error: Boolean,
    val message: String,
    val body: List<ImageItem>
)

@Serializable
data class ImageItem(
    val urls: UrlData,
    val width: Int,
    val height: Int
)

@Serializable
data class UrlData(
    val thumb_mini: String,
    val small: String,
    val regular: String,
    val original: String
)

@Serializable
data class PixivPictureDetailResponse(
    val error: Boolean,
    val message: String,
    val body: PixivIllustDetail
)

@Serializable
data class PixivIllustDetail(
    val illustId: String,
    val title: String,
    val userId: String,
    val userName: String,
    val urls: UrlData,
    val pageCount: Int,
    val width: Int,
    val height: Int
)
