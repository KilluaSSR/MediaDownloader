package killua.dev.mediadownloader.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.mediadownloader.Model.MediaType
import killua.dev.mediadownloader.R
import javax.inject.Inject


object NotificationUtils {
    fun checkPermission(context: Context) =
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED


    fun requestPermission(context: Context) {
        ActivityCompat.requestPermissions(
            context.getActivity(),
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            1
        )
    }
}

class ShowNotification @Inject constructor(
    @ApplicationContext private val context: Context
){
    private val channelId = "download_channel"
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Download Notification",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Show download's progress and status"
            setShowBadge(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showDownloadProgress(downloadId: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Downloading")
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun showDownloadComplete(downloadId: String, fileUri: Uri, name: String, type: MediaType) {
        val (mimeType, notificationTitle) = when (type) {
            MediaType.VIDEO -> "video/*" to "$name's video is ready"
            MediaType.PHOTO -> "image/*" to "$name's photo is ready"
            MediaType.GIF -> "image/*" to "$name's photo is ready"
            MediaType.PDF -> "application/pdf" to "$name is ready"
            MediaType.TXT -> "text/*" to "$name is ready"
            MediaType.M4A -> "audio/mp4" to "$name is ready"
        }
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            downloadId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(notificationTitle)
            .setContentText("Click to open")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun showDownloadFailed(downloadId: String, error: String?) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Download failed")
            .setContentText(error ?: "ERROR")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(downloadId.hashCode(), notification)
    }

    fun showStartGettingLofterImages() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("Getting images from lofter...")
            .setProgress(0,0,true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(LOFTER_GET_BY_TAGS_ID, notification)
    }

    fun updateGettingTweetsProgress(photoCount: Int, videoCount: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Getting tweets")
            .setContentText("$photoCount photos, $videoCount videos detected")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(BOOKMARK_NOTIFICATION_ID, notification)
    }

    fun updateGettingProgress(chapter: String, downloadId: Int = KUAIKAN_ENTIRE_NOTIFICATION_ID, type: String = "comics") {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Getting $type")
            .setContentText("Current: Trying $chapter")
            .setProgress(0,0,true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(downloadId, notification)
    }

    fun completeGettingProgress(totalPhotoCount: Int, totalVideoCount: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Completed!")
            .setContentText("Total: $totalPhotoCount photos, $totalVideoCount videos")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.icon)
            .build()

        notificationManager.notify(BOOKMARK_NOTIFICATION_ID, notification)
    }

    fun cancelNotification(downloadId: String) {
        notificationManager.cancel(downloadId.hashCode())
    }
    fun cancelSpecificNotification(Id: Int){
        notificationManager.cancel(Id)
    }
}

const val BOOKMARK_NOTIFICATION_ID = 10086
const val KUAIKAN_ENTIRE_NOTIFICATION_ID = 10087
const val PIXIV_ENTIRE_NOTIFICATION_ID = 10088
const val LOFTER_GET_BY_TAGS_ID = 10089