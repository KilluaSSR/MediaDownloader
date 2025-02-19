package killua.dev.mediadownloader.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
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
import killua.dev.mediadownloader.Model.FavouriteUserInfo
import killua.dev.mediadownloader.Model.NotLoggedInPlatform
import killua.dev.mediadownloader.Model.SupportedUrlType
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.MainRoutes
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.ViewModels.MainPageUIIntent
import killua.dev.mediadownloader.ui.ViewModels.MainPageViewmodel
import killua.dev.mediadownloader.ui.components.FavouriteCard
import killua.dev.mediadownloader.ui.components.MainPageBottomSheet
import killua.dev.mediadownloader.ui.components.MainPageButtons
import killua.dev.mediadownloader.ui.components.NotLoggedInAlert
import killua.dev.mediadownloader.ui.components.ReportDialog
import killua.dev.mediadownloader.ui.components.common.ActionsBotton
import killua.dev.mediadownloader.ui.components.common.MainInputDialog
import killua.dev.mediadownloader.ui.components.common.MainScaffold
import killua.dev.mediadownloader.ui.components.common.MainTopBar
import killua.dev.mediadownloader.ui.components.common.Section
import killua.dev.mediadownloader.ui.components.common.paddingTop
import killua.dev.mediadownloader.ui.getRandomColors
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainPage(
) {
    val randomColors = getRandomColors()
    val navController = LocalNavController.current!!
    val viewmodel : MainPageViewmodel = hiltViewModel()
    var showDialog by remember { mutableStateOf(false) }
    val uiState = viewmodel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showDevelopingAlert by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    MainScaffold(
        topBar = {
            MainTopBar(navController) { showBottomSheet = true }
        },
        snackbarHostState = viewmodel.snackbarHostState
    ) {
        if(showBottomSheet){
            MainPageBottomSheet(
                onDismiss = {showBottomSheet = false},
                sheetState = sheetState,
                showDevelopingAlert = { showDevelopingAlert = true },
                onShowReport = {showReportDialog = true}
            )
        }

        if (showReportDialog){
            ReportDialog(stringResource(R.string.report),icon = null, onDismiss = {showReportDialog = false})
        }

        if(uiState.value.showNotLoggedIn.showNotLoggedInAlert){
            NotLoggedInAlert(uiState.value.showNotLoggedIn.platforms!!, navController) {
                scope.launch{
                    viewmodel.emitState(uiState.value.copy(showNotLoggedIn = NotLoggedInPlatform()))
                }
            }
        }
        MainInputDialog(
            title = stringResource(R.string.url_here),
            placeholder = "https://...",
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = { url ->
                viewmodel.launchOnIO {
                    when {
                        url.isBlank() -> {
                            viewmodel.emitEffect(SnackbarUIEffect.ShowSnackbar(context.getString(R.string.paste_url_here_)))
                        }
                        else -> {
                            val urlType = SupportedUrlType.fromUrl(url)
                            when(urlType){
                                SupportedUrlType.UNKNOWN -> {
                                    viewmodel.emitEffect(SnackbarUIEffect.ShowSnackbar(context.getString(R.string.unsupported_url)))
                                }
                                else -> {
                                    SupportedUrlType.toPlatform(urlType).let {
                                        viewmodel.emitIntent(MainPageUIIntent.ExecuteDownload(url,it!!))
                                    }
                                }
                            }
                        }

                    }
                }
            }
        )
        Column(
            modifier = Modifier
                .paddingTop(SizeTokens.Level8)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Section(title = stringResource(R.string.overview)) {
                if(uiState.value.youHaveDownloadedSth) {
                    val userInfo = FavouriteUserInfo(
                        name = uiState.value.favouriteUserName,
                        screenName = uiState.value.favouriteUserScreenName,
                        downloadCount = uiState.value.downloadedTimes,
                        platform = uiState.value.favouriteUserFromPlatform,
                        hasDownloaded = true
                    )

                    FavouriteCard(
                        userInfo = userInfo,
                        onClick = {
                            viewmodel.launchOnIO {
                                viewmodel.emitIntent(
                                    MainPageUIIntent.NavigateToFavouriteUser(
                                        context,
                                        uiState.value.favouriteUserID,
                                        uiState.value.favouriteUserFromPlatform,
                                        uiState.value.favouriteUserScreenName
                                    )
                                )
                            }
                        }
                    )
                } else {
                    FavouriteCard(
                        userInfo = FavouriteUserInfo(
                            platform = uiState.value.favouriteUserFromPlatform
                        )
                    )
                }
            }

            Section(title = stringResource(R.string.actions)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    MainPageButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = context.getString(item.titleRes),
                            icon = item.icon,
                            color = randomColors[index].container
                        ) {
                            if (item.route == MainRoutes.Download.route) {
                                showDialog = true
                            } else {
                                navController.navigateSingle(item.route)
                            }
                        }
                    }
                }
            }

        }
    }
}
