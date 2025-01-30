package killua.dev.twitterdownloader.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

fun Context.NavigateTwitterProfile(userID: String? = null, screenName: String){
    val intent = try {
        if(isTwitterInstalled()){
            if(!userID.isNullOrEmpty()){
                Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=$userID"))
            }else{
                Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=$screenName"))
            }
        }else{
            Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/$screenName"))
        }
    }catch (e: Exception){
        Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/$screenName"))
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.NavigateTwitterTweet(userScreenName:String, tweetID: String? = null){
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/$userScreenName/status/$tweetID"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.isTwitterInstalled(): Boolean = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo("com.twitter.android", PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo("com.twitter.android", 0)
    }
    true
} catch (e: Exception) {
    false
}