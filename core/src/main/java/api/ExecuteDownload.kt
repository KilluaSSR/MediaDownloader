package api

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import api.Model.Tweet
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

enum class MediaType {
    photo,
    video,
    animated_gif
}
class ExecuteDownload @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun Preprocessing(tweets: List<Tweet>?) {
        tweets?.forEach { tweet ->
            tweet.media.forEach { media ->
//                when (media.type) {
//                    MediaType.photo.name -> downloadPhoto(media.url)
//                    MediaType.video.name -> downloadVideo(media.url, media.bitrate)
//                    MediaType.animated_gif.name -> downloadGif(media.url)
//                }
                DownloadImage(media.url.toString(), tweet.userId!!).let { it -> if (it != null) ImageSaver(context, it, tweet.userId!!) }
            }
        }
    }
    private suspend fun DownloadImage(imgURL: String, userID: String): Bitmap? = withContext(Dispatchers.IO){
        try {
            val url = URL(imgURL)
            (url.openConnection() as HttpURLConnection).apply {
                setRequestProperty("User-Agent", "Mozilla/5.0")
                setRequestProperty("Referer", imgURL)
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }.inputStream.use { inputStream ->
                return@withContext BitmapFactory.decodeStream(inputStream)
            }
        }catch (ex: Exception){
            println("${ex.message}")
            null
        }
    }
    private suspend fun ImageSaver(context: Context,bitmap: Bitmap, userID: String) = withContext(Dispatchers.IO){
        val displayName = "id=$userID + time=${System.currentTimeMillis()}.jpg"
        val mimeType = "image/jpeg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }else{
                put(MediaStore.MediaColumns.DATA, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString())
            }
        }
        val resolver = context.contentResolver
        val stream: OutputStream?
        val uri: Uri?
        try {
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            uri = resolver.insert(contentUri, contentValues)
            if (uri != null){
                stream = resolver.openOutputStream(uri)
                if (stream != null){
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    stream.close()
                }
            }
        }catch (ex: Exception){
            println("${ex.message}")
        }
    }
}