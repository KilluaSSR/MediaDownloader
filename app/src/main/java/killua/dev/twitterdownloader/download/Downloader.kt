package killua.dev.twitterdownloader.download

import android.net.Uri
import killua.dev.base.Model.DownloadTask

interface Downloader {
    suspend fun download(
        task: DownloadTask,
        headers: Map<String, String> = emptyMap()
    ): Result<Uri>
}