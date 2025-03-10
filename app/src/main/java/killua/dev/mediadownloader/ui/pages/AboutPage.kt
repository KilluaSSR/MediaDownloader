package killua.dev.mediadownloader.ui.pages

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
import androidx.compose.ui.res.stringResource
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.components.common.Clickable
import killua.dev.mediadownloader.ui.components.common.SettingsScaffold
import killua.dev.mediadownloader.ui.components.common.Title
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.drawableToImageVector

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AboutPage() {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    rememberCoroutineScope()
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.about),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Title(title = stringResource(R.string.about), color = MaterialTheme.colorScheme.primary) {
                Clickable(
                    title = context.getString(R.string.developer_name),
                    value = "KilluaDev.kt"
                ) {

                }
                Clickable(
                    icon = drawableToImageVector(R.drawable.logo_of_twitter),
                    title = stringResource(R.string.twitter),
                    value = "@Shakeitoff_pi"
                ) {
                    val twitterUrl = "https://x.com/${context.getString(R.string.twitterAccount)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(twitterUrl)).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
                Clickable(
                    icon = drawableToImageVector(R.drawable.github_mark),
                    title = "Github",
                    value = "@KilluaSSR"
                ) {
                    val githubUrl = "https://github.com/${context.getString(R.string.twitterAccount)}"
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