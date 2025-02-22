package killua.dev.mediadownloader

import android.app.Application
import androidx.startup.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import killua.dev.mediadownloader.download.DownloadManager
import killua.dev.mediadownloader.download.DownloadQueueManager
import killua.dev.mediadownloader.download.MediaDownloaderFactory
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitializer.getInstance(this)
            .initializeComponent(ServicesInitializer::class.java)
    }

}