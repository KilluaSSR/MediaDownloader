package killua.dev.twitterdownloader

import android.app.Application
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    companion object{
        lateinit var application: Application
    }
    override fun onCreate() {
        super.onCreate()
        application = this
    }

}