package killua.dev.mediadownloader.ui.pages

import android.content.Intent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import killua.dev.base.ui.components.Clickable
import killua.dev.base.ui.components.SettingsScaffold
import killua.dev.base.ui.components.Slideable
import killua.dev.base.ui.components.Switchable
import killua.dev.base.ui.components.Title
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.ActivityUtil
import killua.dev.base.utils.getActivity
import killua.dev.base.utils.navigateSingle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import killua.dev.mediadownloader.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(){
    val context = LocalContext.current
    val navController =  LocalNavController.current!!
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.settings),
    ) {
        var isShowReset by remember { mutableStateOf(false) }
        if(isShowReset){
            CancellableAlert(
                title = stringResource(R.string.reset_title),
                mainText = stringResource(R.string.reset_desc),
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
                    title = stringResource(R.string.notification),
                    checkedText = stringResource(R.string.notification_desc)
                )
                Switchable(
                    key = WIFI,
                    title = stringResource(R.string.wifi_only),
                    checkedText = if(wifi){stringResource(R.string.wifi_only_enabled)} else{stringResource(R.string.wifi_only_disabled)}
                )
                Switchable(
                    key = PHOTOS_KEY,
                    title = stringResource(R.string.twitter_images),
                    checkedText = if(photos){stringResource(R.string.twitter_images_enabled)} else{stringResource(R.string.wifi_only_disabled)}
                )
                Slideable(
                    title = stringResource(R.string.max_cocurrent_downloads),
                    value = concurrent.toFloat(),
                    valueRange = 1F..10F,
                    steps = 8,
                    desc = remember(concurrent) {"${context.getString(R.string.current)}:" +concurrent}
                ) {
                    scope.launch{
                        context.writeMaxConcurrentDownloads(it.roundToInt())
                    }
                }

                Slideable(
                    title = stringResource(R.string.max_retries),
                    value = retry.toFloat(),
                    valueRange = 0F..3F,
                    steps = 2,
                    desc = remember(retry) {"${context.getString(R.string.current)}:" + retry }
                ) {
                    scope.launch{
                        context.writeMaxRetries(it.roundToInt())
                    }
                }

                Slideable(
                    title = stringResource(R.string.delay),
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

            Title(title = stringResource(R.string.platform_configurations), color = MaterialTheme.colorScheme.error) {
                Clickable(
                    title = "Twitter",
                    value = "Log in / out."
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
                    }
                }
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
                    title = stringResource(R.string.kuaikan),
                    value = "Log in / out."
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.KuaikanPreparePage.route)
                    }
                }
            }

            Title(title = stringResource(R.string.dangerous), color = MaterialTheme.colorScheme.error) {

                Clickable(
                    title = stringResource(R.string.reset),
                    value = stringResource(R.string.reset_desc)
                ){
                    isShowReset = true
                }
            }

        }
    }
}