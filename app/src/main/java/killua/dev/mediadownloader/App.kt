package killua.dev.mediadownloader

import android.app.Application
import androidx.startup.AppInitializer
import dagger.hilt.android.HiltAndroidApp

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