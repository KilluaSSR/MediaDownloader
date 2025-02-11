package killua.dev.twitterdownloader.api.Pixiv.BuildRequest

import killua.dev.base.utils.USER_AGENT
import okhttp3.Request

object PixivHeaders {
    const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"

    fun getPictureHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.pixiv.net/artworks/$id"
    )
}

fun Request.Builder.addPixivPictureFetchHeaders(id: String): Request.Builder = apply {
    PixivHeaders.getPictureHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}

fun <T : Request.Builder> T.addPixivPictureDownloadHeaders(): T {
    this.addHeader("User-Agent", USER_AGENT)
        .addHeader("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
        .addHeader("Referer", "https://www.pixiv.net")
    return this
}