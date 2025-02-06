package killua.dev.twitterdownloader.download

import android.net.Uri
import killua.dev.base.Model.DownloadTask

interface MediaDownloader {
    suspend fun download(
        task: DownloadTask,
        headers: Map<String, String> = emptyMap(),
        onProgress: (Int) -> Unit
    ): Result<Uri>
}