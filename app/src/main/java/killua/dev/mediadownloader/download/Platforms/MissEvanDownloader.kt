package killua.dev.mediadownloader.download.Platforms

import android.content.Context
import killua.dev.mediadownloader.Model.DownloadTask
import killua.dev.mediadownloader.download.BaseMediaDownloader
import killua.dev.mediadownloader.utils.MediaStoreHelper
import killua.dev.mediadownloader.utils.USER_AGENT
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class MissEvanDownloader(
    context: Context,
    mediaHelper: MediaStoreHelper
) : BaseMediaDownloader(context, mediaHelper) {
    override fun buildClient(headers: Map<String, String>) = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun getHeaders(task: DownloadTask)  = mapOf(
        "user-agent" to USER_AGENT
    )
}