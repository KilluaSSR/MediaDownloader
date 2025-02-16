package killua.dev.mediadownloader.utils

import android.app.Activity
import android.content.Context

fun Context.getActivity() = this as Activity

object ActivityUtil {
    val classMainActivity: Class<*> = Class.forName("killua.dev.mediadownloader.MainActivity")
    val SetupActivity: Class<*> = Class.forName("killua.dev.mediadownloader.Setup.SetupActivity")
}