package killua.dev.mediadownloader.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

fun Context.navigateTwitterProfile(userID: String? = null, screenName: String){
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

fun Context.navigateLofterProfile(screenName: String){
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://$screenName.lofter.com/"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.navigatePixivProfile(userID: String? = null){
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.pixiv.net/users/$userID"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.navigateTwitterTweet(userScreenName:String?, tweetID: String? = null, url: String?){
    val intent = if(userScreenName?.isNotBlank() == true && tweetID?.isNotBlank() == true){
        Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/$userScreenName/status/$tweetID"))
    }else{
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.navigateToLink(link: String? = null){
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.isTwitterInstalled(): Boolean = try {
    packageManager.getPackageInfo("com.twitter.android", PackageManager.PackageInfoFlags.of(0))
    true
} catch (e: Exception) {
    false
}