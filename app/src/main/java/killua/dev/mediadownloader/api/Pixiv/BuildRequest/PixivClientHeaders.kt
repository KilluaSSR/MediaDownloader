package killua.dev.mediadownloader.api.Pixiv.BuildRequest

import killua.dev.mediadownloader.utils.USER_AGENT
import okhttp3.Request

object PixivHeaders {

    fun getPictureHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.pixiv.net/artworks/$id"
    )

    fun getNovelHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.pixiv.net/novel/show.php?id=$id"
    )

    fun getEntireNovelHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.pixiv.net/novel/series/$id"
    )
}

fun Request.Builder.addPixivPictureFetchHeaders(id: String): Request.Builder = apply {
    PixivHeaders.getPictureHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}

fun Request.Builder.addPixivNovelFetchHeaders(id: String): Request.Builder = apply {
    PixivHeaders.getNovelHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}

fun Request.Builder.addPixivEntireNovelHeaders(id: String): Request.Builder = apply {
    PixivHeaders.getEntireNovelHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}

fun <T : Request.Builder> T.addPixivPictureDownloadHeaders(): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
        .addHeader("Referer", "https://www.pixiv.net")
    return this
}