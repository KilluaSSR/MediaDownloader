package killua.dev.mediadownloader

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import killua.dev.mediadownloader.download.DownloadManager
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.download.MediaDownloaderFactory

class ServicesInitializer : Initializer<Unit> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ServicesInitializerEntryPoint {
        fun downloadQueueManager(): DownloadQueueManager
        fun downloadManager(): DownloadManager
        fun mediaDownloaderFactory(): MediaDownloaderFactory
    }

    override fun create(context: Context) {
        Log.d(TAG, "Starting services initialization")

        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            ServicesInitializerEntryPoint::class.java
        )

        try {
            // 获取所需的服务实例
            entryPoint.downloadQueueManager()
            entryPoint.downloadManager()
            entryPoint.mediaDownloaderFactory()

            Log.d(TAG, "Services initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Services initialization failed", e)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

    companion object {
        private const val TAG = "ServicesInitializer"
    }
}