package killua.dev.twitterdownloader.core.utils

import android.content.Context
import android.os.Environment
import java.io.File

object FileUtils {
    private const val APP_FOLDER_NAME = "TwitterDownloader"
    private const val VIDEOS_FOLDER_NAME = "twitter_videos"

    fun getAppBaseDir(context: Context): File {
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, APP_FOLDER_NAME)
        val videosDir = File(appDir, VIDEOS_FOLDER_NAME)

        if (!videosDir.exists()) {
            videosDir.mkdirs()
        }
        return videosDir
    }

    fun getUserVideoDir(context: Context, screenName: String): File {
        val baseDir = getAppBaseDir(context)
        val userDir = File(baseDir, screenName)
        if (!userDir.exists()) {
            userDir.mkdirs()
        }
        return userDir
    }
}