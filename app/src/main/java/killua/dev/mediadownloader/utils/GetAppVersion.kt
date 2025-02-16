package killua.dev.mediadownloader.utils

import android.content.Context

fun getAppVersion(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "Unknown"
    }.toString()
}