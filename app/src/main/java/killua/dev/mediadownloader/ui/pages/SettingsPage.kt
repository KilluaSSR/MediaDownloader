package killua.dev.mediadownloader.ui.pages

import android.annotation.SuppressLint
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
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.datastore.NOTIFICATION_ENABLED
import killua.dev.mediadownloader.datastore.PHOTOS_KEY
import killua.dev.mediadownloader.datastore.SECURE_MY_DOWNLOAD
import killua.dev.mediadownloader.datastore.WIFI
import killua.dev.mediadownloader.datastore.readDelay
import killua.dev.mediadownloader.datastore.readDownloadPhotos
import killua.dev.mediadownloader.datastore.readMaxConcurrentDownloads
import killua.dev.mediadownloader.datastore.readMaxRetries
import killua.dev.mediadownloader.datastore.readOnlyWifi
import killua.dev.mediadownloader.datastore.readSecureMyDownload
import killua.dev.mediadownloader.datastore.readTheme
import killua.dev.mediadownloader.datastore.writeApplicationUserAuth
import killua.dev.mediadownloader.datastore.writeApplicationUserCt0
import killua.dev.mediadownloader.datastore.writeApplicationUserID
import killua.dev.mediadownloader.datastore.writeDelay
import killua.dev.mediadownloader.datastore.writeKuaikanPassToken
import killua.dev.mediadownloader.datastore.writeLofterLoginAuth
import killua.dev.mediadownloader.datastore.writeLofterLoginKey
import killua.dev.mediadownloader.datastore.writeMaxConcurrentDownloads
import killua.dev.mediadownloader.datastore.writeMaxRetries
import killua.dev.mediadownloader.datastore.writePixivPHPSSID
import killua.dev.mediadownloader.datastore.writeTheme
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.ViewModels.SettingsPageViewModel
import killua.dev.mediadownloader.ui.components.ThemeSettingsBottomSheet
import killua.dev.mediadownloader.ui.components.common.CancellableAlert
import killua.dev.mediadownloader.ui.components.common.Clickable
import killua.dev.mediadownloader.ui.components.common.SettingsScaffold
import killua.dev.mediadownloader.ui.components.common.Slideable
import killua.dev.mediadownloader.ui.components.common.Switchable
import killua.dev.mediadownloader.ui.components.common.SwitchableSecured
import killua.dev.mediadownloader.ui.components.common.Title
import killua.dev.mediadownloader.ui.theme.ThemeMode
import killua.dev.mediadownloader.ui.theme.getThemeModeName
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.ActivityUtil
import killua.dev.mediadownloader.utils.BiometricManagerSingleton
import killua.dev.mediadownloader.utils.getActivity
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@SuppressLint("StringFormatMatches")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun SettingsPage(){
    val context = LocalContext.current
    val navController = LocalNavController.current!!
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    val viewModel: SettingsPageViewModel = hiltViewModel()
    var showThemeMenu by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val currentTheme by context.readTheme()
        .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM.name)
    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.settings),
        snackbarHostState = viewModel.snackbarHostState
    ) {
        if (showThemeMenu){
            ThemeSettingsBottomSheet(
                onDismiss = { showThemeMenu = false },
                sheetState = sheetState,
                onThemeSelected = { theme -> context.writeTheme(theme.name) }
            )
        }
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
            val securedDownloadList by context.readSecureMyDownload().collectAsStateWithLifecycle(initialValue = false)
            val photos by context.readDownloadPhotos().collectAsStateWithLifecycle(initialValue = true)
            val delay by context.readDelay().collectAsStateWithLifecycle(initialValue = 2)
            val isBiometricAvailable = remember {
                BiometricManagerSingleton.getBiometricHelper()?.canAuthenticate() == true
            }

            Title(title = stringResource(R.string.download)) {
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
                    checkedText = if(photos){stringResource(R.string.twitter_images_enabled)} else{stringResource(R.string.twitter_images_disabled)}
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
                    desc = remember(delay) {context.getString(R.string.delay_desc, delay)}
                ) {
                    scope.launch{
                        context.writeDelay(it.roundToInt())
                    }
                }
            }
            Title(title = stringResource(R.string.application_settings)) {
                Clickable(
                    title = stringResource(R.string.theme),
                    value = getThemeModeName(ThemeMode.valueOf(currentTheme))
                ) {
                    showThemeMenu = true
                }
            }
            Title(title = stringResource(R.string.privacy)) {
                SwitchableSecured(
                    enabled = isBiometricAvailable,
                    key = SECURE_MY_DOWNLOAD,
                    title = stringResource(R.string.biometric_auth),
                    checkedText = when {
                        !isBiometricAvailable -> stringResource(R.string.biometric_auth_disabled_desc)
                        securedDownloadList -> stringResource(R.string.biometric_auth_desc_on)
                        else -> stringResource(R.string.biometric_auth_desc_off)
                    },
                ){ errorMsg->
                    scope.launch {
                        viewModel.emitEffect(
                            SnackbarUIEffect.ShowSnackbar(
                                errorMsg
                            )
                        )
                    }
                }
            }

            Title(title = stringResource(R.string.platform_configurations)) {
                Clickable(
                    title = stringResource(R.string.twitter),
                    value = stringResource(R.string.log_in_out)
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
                    }
                }
                Clickable(
                    title = stringResource(R.string.lofter),
                    value = stringResource(R.string.lofter_settings_desc),
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
                    }
                }

                Clickable(
                    title = stringResource(R.string.pixiv),
                    value = stringResource(R.string.log_in_out)
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.PixivPreparePage.route)
                    }
                }
                Clickable(
                    title = stringResource(R.string.kuaikan),
                    value = stringResource(R.string.log_in_out)
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.KuaikanPreparePage.route)
                    }
                }
                Clickable(
                    title = stringResource(R.string.missevan),
                    value = stringResource(R.string.log_in_out)
                ){
                    scope.launch{
                        navController.navigateSingle(PrepareRoutes.MissEvavnPreparePage.route)
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