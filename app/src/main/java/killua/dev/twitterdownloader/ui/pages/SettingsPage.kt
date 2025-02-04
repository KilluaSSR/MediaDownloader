package killua.dev.twitterdownloader.ui.pages

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.datastore.NOTIFICATION_ENABLED
import killua.dev.base.datastore.PHOTOS_KEY
import killua.dev.base.datastore.WIFI
import killua.dev.base.datastore.readDownloadPhotos
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readOnlyWifi
import killua.dev.base.datastore.writeMaxConcurrentDownloads
import killua.dev.base.datastore.writeMaxRetries
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.SettingsScaffold
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.ui.components.Clickable
import killua.dev.base.ui.components.Slideable
import killua.dev.base.ui.components.Switchable
import killua.dev.base.ui.components.Title
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(){
    val context = LocalContext.current
    LocalNavController.current!!
    //val viewModel = hiltViewModel<IndexViewModel>()
    //val directoryState by viewModel.directoryState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = "Settings",
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            val scope = rememberCoroutineScope()
            val concurrent by context.readMaxConcurrentDownloads().collectAsStateWithLifecycle(initialValue = 3)
            val retry by context.readMaxRetries().collectAsStateWithLifecycle(initialValue = 3)
            val wifi by context.readOnlyWifi().collectAsStateWithLifecycle(initialValue = true)
            val photos by context.readDownloadPhotos().collectAsStateWithLifecycle(initialValue = false)
            Title(title = "Download") {
                Switchable(
                    key = NOTIFICATION_ENABLED,
                    title = "Notification",
                    checkedText = "Send notification after download"
                )
                Switchable(
                    key = WIFI,
                    title = "Download via WIFI only",
                    checkedText = if(wifi){"Download is disabled if you're using Cellar Data"} else{"Extra carrier charges may apply"}
                )
                Switchable(
                    key = PHOTOS_KEY,
                    title = "Download images",
                    checkedText = if(photos){"Download images too"} else{"Download videos only"}
                )
                Slideable(
                    title = "Max concurrent downloads",
                    value = concurrent.toFloat(),
                    valueRange = 1F..10F,
                    steps = 8,
                    desc = remember(concurrent) {"Current: $concurrent"}
                ) {
                    scope.launch{
                        context.writeMaxConcurrentDownloads(it.roundToInt())
                    }
                }

                Slideable(
                    title = "Max retries",
                    value = retry.toFloat(),
                    valueRange = 0F..3F,
                    steps = 2,
                    desc = remember(retry) {"Current: $retry"}
                ) {
                    scope.launch{
                        context.writeMaxRetries(it.roundToInt())
                    }
                }
            }

            Title(title = "Dangerous", color = MaterialTheme.colorScheme.error) {

                Clickable(
                    icon = Icons.Outlined.Delete,
                    title = "Reset",
                    desc = "Clear your login information (cookies). You need to login again."
                ){

                }
            }

        }
    }
}