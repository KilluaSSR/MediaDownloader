package killua.dev.twitterdownloader.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.utils.getAppVersion
import killua.dev.twitterdownloader.R

fun openMail(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${context.getString(R.string.mail)}")
        putExtra(Intent.EXTRA_SUBJECT, "App Feedback")
        putExtra(Intent.EXTRA_TEXT, """
            Device Information:
            Android Version: ${Build.VERSION.RELEASE}
            Device Model: ${Build.MODEL}
            App Version: ${getAppVersion(context)}
            Description:
        
            
            Steps to Reproduce:
            1. 
            2. 
            3. 
        
            Expected Behavior:
        
        
            Actual Behavior:
        
        
            Additional Information:
            
        """.trimIndent()) // 邮件正文模板
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        SnackbarUIEffect.ShowSnackbar("Mail application not found")
    }
}

fun openGithubIssues(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW,
        Uri.parse("https://github.com/${context.getString(R.string.githubAccount)}/TwitterDownloader/issues")
    )
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        SnackbarUIEffect.ShowSnackbar("Browser application not found")
    }
}