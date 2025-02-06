package killua.dev.twitterdownloader.ui.pages

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.MainPageButtons
import killua.dev.base.ui.MainRoutes
import killua.dev.base.ui.SnackbarUIEffect
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.DevelopingAlert
import killua.dev.base.ui.components.MainScaffold
import killua.dev.base.ui.components.MainTopBar
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.getRandomColors
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.Model.MainPageUIIntent
import killua.dev.twitterdownloader.ui.ViewModels.MainPageViewmodel
import killua.dev.twitterdownloader.ui.components.FavouriteCard
import killua.dev.twitterdownloader.ui.components.MainPageBottomSheet
import killua.dev.twitterdownloader.ui.components.ReportDialog
import killua.dev.twitterdownloader.ui.components.URLInputDialog

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
    rememberCoroutineScope()
    var showDevelopingAlert by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    MainScaffold(
        topBar = {
            MainTopBar(navController, {showBottomSheet = true})
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
        if (showDevelopingAlert) {
            DevelopingAlert(
                onDismiss = { showDevelopingAlert = false }
            )
        }
        if (showReportDialog){
            ReportDialog("Report",icon = null, onDismiss = {showReportDialog = false})
        }
        URLInputDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = { url ->
                viewmodel.launchOnIO{
                    if(url.isBlank()){
                        viewmodel.emitEffect(SnackbarUIEffect.ShowSnackbar("You need to paste a url here."))
                    }else if(!url.contains("x.com/") && !url.contains("twitter.com/") && !url.contains("lofter")) {
                        viewmodel.emitEffect(SnackbarUIEffect.ShowSnackbar("Unsupported url"))
                    }else {
                        val tweetID = url.split("?")[0].split("/").last()
                        viewmodel.emitIntent(MainPageUIIntent.ExecuteDownload(tweetID))
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
            Section(title = "Overview") {
                if(uiState.value.youHaveDownloadedSth){
                    FavouriteCard(uiState.value.favouriteUserName, uiState.value.favouriteUserScreenName, uiState.value.downloadedTimes,true){
                        viewmodel.launchOnIO {
                            viewmodel.emitIntent(MainPageUIIntent.NavigateToFavouriteUser(context,uiState.value.favouriteUserID,uiState.value.favouriteUserScreenName))
                        }
                    }
                }else{
                    FavouriteCard("", "", 0,false) {}
                }
            }

            Section(title = "Actions") {
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
                            title = item.title,
                            icon = item.icon,
                            color = randomColors[index].container
                        ) {
                            if (item.route == MainRoutes.Download.route) {
                                showDialog = true
                            } else {
                                if (item.route == MainRoutes.DownloadPage.route){
                                    navController.navigateSingle(item.route)
                                }else{
                                    showDevelopingAlert = true
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
