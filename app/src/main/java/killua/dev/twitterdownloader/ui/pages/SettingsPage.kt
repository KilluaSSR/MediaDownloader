package killua.dev.twitterdownloader.ui.pages

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.datastore.NOTIFICATION_ENABLED
import killua.dev.base.datastore.PHOTOS_KEY
import killua.dev.base.datastore.WIFI
import killua.dev.base.datastore.readDelay
import killua.dev.base.datastore.readDownloadPhotos
import killua.dev.base.datastore.readMaxConcurrentDownloads
import killua.dev.base.datastore.readMaxRetries
import killua.dev.base.datastore.readOnlyWifi
import killua.dev.base.datastore.writeApplicationUserAuth
import killua.dev.base.datastore.writeApplicationUserCt0
import killua.dev.base.datastore.writeApplicationUserID
import killua.dev.base.datastore.writeDelay
import killua.dev.base.datastore.writeKuaikanPassToken
import killua.dev.base.datastore.writeLofterLoginAuth
import killua.dev.base.datastore.writeLofterLoginKey
import killua.dev.base.datastore.writeMaxConcurrentDownloads
import killua.dev.base.datastore.writeMaxRetries
import killua.dev.base.datastore.writePixivPHPSSID
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.PrepareRoutes
import killua.dev.base.ui.components.CancellableAlert
import killua.dev.base.ui.components.SettingsScaffold
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.ui.components.Clickable
import killua.dev.base.ui.components.Slideable
import killua.dev.base.ui.components.Switchable
import killua.dev.base.ui.components.Title
import killua.dev.base.utils.ActivityUtil
import killua.dev.base.utils.getActivity
import killua.dev.base.utils.navigateSingle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(){
    val context = LocalContext.current
    val navController =  LocalNavController.current!!
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = "Settings",
    ) {
        var isShowReset by remember { mutableStateOf(false) }
        if(isShowReset){
            CancellableAlert(
                title = "Reset now?",
                mainText = "Your login information will be cleared, and you will need to log in again to continue using",
                onDismiss = {isShowReset = false},
                icon = {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null ,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .size(SizeTokens.Level48))
                }
            ) {
                scope.launch{
                    context.writeApplicationUserCt0("")
                    context.writeApplicationUserAuth("")
                    context.writeApplicationUserID("")
                    context.writeLofterLoginAuth("")
                    context.writeLofterLoginKey("")
                    context.writePixivPHPSSID("")
                    context.writeKuaikanPassToken("")
                    context.startActivity(Intent(context, ActivityUtil.SetupActivity))
                    context.getActivity().finish()
                }
            }
        }
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
            val photos by context.readDownloadPhotos().collectAsStateWithLifecycle(initialValue = true)
            val delay by context.readDelay().collectAsStateWithLifecycle(initialValue = 2)
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
                    title = "Download Twitter images",
                    checkedText = if(photos){"Download twitter images too. This setting is only effective when you download using the link."} else{"Download twitter videos only. This setting is only effective when you download using the link."}
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

                Slideable(
                    title = "Delay",
                    value = delay.toFloat(),
                    valueRange = 2F..10F,
                    steps = 7,
                    desc = remember(delay) {"Current: $delay seconds. This means that when retrieving images in bulk, there should be a $delay-second interval between each page request."}
                ) {
                    scope.launch{
                        context.writeDelay(it.roundToInt())
                    }
                }
            }

            Title(title = "Platform configurations", color = MaterialTheme.colorScheme.error) {

                Clickable(
                    title = "Lofter",
                    value = "Date range, Tags and Log in / out."
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
                    }
                }

                Clickable(
                    title = "Pixiv",
                    value = "Log in / out."
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.PixivPreparePage.route)
                    }
                }
                Clickable(
                    title = "Kuaikan",
                    value = "Log in / out."
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.KuaikanPreparePage.route)
                    }
                }
            }

            Title(title = "Dangerous", color = MaterialTheme.colorScheme.error) {

                Clickable(
                    title = "Reset",
                    value = "Clear your login information (cookies). You need to login again."
                ){
                    isShowReset = true
                }
            }

        }
    }
}