package killua.dev.mediadownloader.api.MissEvan.BuildRequest

import killua.dev.mediadownloader.api.Pixiv.BuildRequest.PixivHeaders
import killua.dev.mediadownloader.utils.USER_AGENT
import okhttp3.Request
import kotlin.collections.component1
import kotlin.collections.component2

object MissEvanHeaders {
    fun getEntireDramaHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.missevan.com/mdrama/$id"
    )
    fun getSoundHeaders(id: String) = mapOf(
        "User-Agent" to USER_AGENT,
        "Accept" to "application/json",
        "Referer" to "https://www.missevan.com/sound/player?id=$id"
    )

}

fun Request.Builder.addMissEvanDramaFetchHeaders(id: String): Request.Builder = apply {
    MissEvanHeaders.getEntireDramaHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}

fun Request.Builder.addMissEvanSoundFetchHeaders(id: String): Request.Builder = apply {
    MissEvanHeaders.getSoundHeaders(id).forEach { (key, value) ->
        addHeader(key, value)
    }
}