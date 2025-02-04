package killua.dev.twitterdownloader.ui.pages

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.Clickable
import killua.dev.base.ui.components.SettingsScaffold
import killua.dev.base.ui.components.Title
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.drawableToImageVector
import killua.dev.twitterdownloader.R
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AboutPage() {
    val context = LocalContext.current
    LocalNavController.current!!
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    rememberCoroutineScope()
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = "About",
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Title(title = "Author", color = MaterialTheme.colorScheme.primary) {
                Clickable(
                    icon = drawableToImageVector(killua.dev.base.R.drawable.logo_of_twitter),
                    title = "风过荒野",
                    value = "KilluaDev.kt"
                ) {

                }
                Clickable(
                    icon = drawableToImageVector(killua.dev.base.R.drawable.logo_of_twitter),
                    title = "Twitter",
                    value = "@Shakeitoff_pi"
                ) {
                    val twitterUrl = "https://x.com/${context.getString(R.string.twitterAccount)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                Clickable(
                    icon = drawableToImageVector(killua.dev.base.R.drawable.github_mark),
                    title = "Github",
                    value = "@Shakeitoff_pi"
                ) {
                    val githubUrl = "https://github.com/${context.getString(R.string.githubAccount)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(githubUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                Clickable(
                    title = "Donate",
                ) {

                }
            }
        }
    }
}