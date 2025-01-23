package killua.dev.base.utils

import android.app.Activity
import android.content.Context

fun Context.getActivity() = this as Activity

object ActivityUtil {
    val classMainActivity: Class<*> = Class.forName("killua.dev.twitterdownloader.MainActivity")
    val SetupActivity: Class<*> = Class.forName("killua.dev.setup.MainActivity")
}