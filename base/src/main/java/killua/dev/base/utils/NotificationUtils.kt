package killua.dev.base.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import killua.dev.base.Model.MediaType
import killua.dev.base.R
import javax.inject.Inject

object NotificationUtils {
    fun checkPermission(context: Context) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }


    fun requestPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                context.getActivity(),
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        } else {
            runCatching {
                val intent = Intent().apply {
                    setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
                }
                context.startActivity(intent)
            }.onFailure {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
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

    fun cancelNotification(downloadId: String) {
        notificationManager.cancel(downloadId.hashCode())
    }
}