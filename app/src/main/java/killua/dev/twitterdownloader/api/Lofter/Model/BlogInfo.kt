package killua.dev.twitterdownloader.api.Lofter.Model

import killua.dev.base.Model.ImageType

data class BlogImage(
    val url: String,
    val filename: String,
    val type: ImageType,
    val blogUrl: String
)

data class BlogInfo(
    val authorName: String,
    val authorId: String,
    val authorDomain: String,
    val images: List<BlogImage>
)

data class ArchiveInfo(
    val imgUrl: String,
    val blogUrl: String,
    val time: String
)