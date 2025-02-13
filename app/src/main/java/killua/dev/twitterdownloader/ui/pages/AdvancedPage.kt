package killua.dev.twitterdownloader.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.twitterdownloader.ui.components.AdvancedPageLofterButtons
import killua.dev.twitterdownloader.ui.components.AdvancedPageTwitterButtons
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.PrepareRoutes
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.AdvancedInputDialog
import killua.dev.base.ui.components.CancellableAlert
import killua.dev.base.ui.components.DevelopingAlert
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.ui.ViewModels.AdvancedPageUIIntent
import killua.dev.twitterdownloader.ui.ViewModels.AdvancedPageViewModel
import killua.dev.twitterdownloader.ui.ViewModels.DialogType
import killua.dev.twitterdownloader.ui.components.AdvancedPageKuaikanButtons
import killua.dev.twitterdownloader.ui.components.AdvancedPageTopAppBar
import killua.dev.twitterdownloader.ui.components.MainScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedPage(){
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel: AdvancedPageViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var showDevelopingAlert by remember { mutableStateOf(false) }
    val eligibleToUseLofterGetByTags = viewModel.lofterGetByTagsEligibility.collectAsStateWithLifecycle()
    var dialogTitle by remember { mutableStateOf("") }
    var dialogPlaceholder by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(AdvancedPageUIIntent.OnEntry(context))
    }
    MainScaffold (
        topBar = {
            AdvancedPageTopAppBar(navController)
        },

    ){
        if (showDevelopingAlert) {
            DevelopingAlert(
                onDismiss = { showDevelopingAlert = false }
            )
        }
        var showGetAllMyTwitterBookmarks by remember { mutableStateOf(false) }
        if(showGetAllMyTwitterBookmarks){
            CancellableAlert(
                title = "Get my Bookmarks",
                mainText = "Get the content I bookmarked on Twitter.",
                onDismiss = {showGetAllMyTwitterBookmarks = false}
            ) {
                scope.launch{
                    viewModel.emitIntent(AdvancedPageUIIntent.GetMyTwitterBookmark)
                }
            }
        }
        var showGetMyTwitterLikes by remember { mutableStateOf(false) }
        if(showGetMyTwitterLikes){
            CancellableAlert(
                title = "Get my Likes",
                mainText = "Get the content I liked on Twitter.\nNote that if you have a lot of liked content, it may trigger a rate limit risk and could even lead to account suspension.",
                onDismiss = {showGetMyTwitterLikes = false}
            ) {
                scope.launch{
                    viewModel.emitIntent(AdvancedPageUIIntent.GetMyTwitterLiked)
                }
            }
        }

        AdvancedInputDialog(
            title = dialogTitle,
            placeholder = dialogPlaceholder,
            showDialog = showDialog,
            loading = uiState.value.isFetching,
            userInfo = if (uiState.value.info.first.isNotEmpty())
                uiState.value.info else null,
            onDismiss = {
                showDialog = false
                scope.launch{
                    viewModel.emitState(uiState.value.copy(isFetching = false,info = Triple("", "", "")))
                }
            },
            onCancel = {
                scope.launch {
                    viewModel.emitState(uiState.value.copy(
                        info = Triple("", "", ""),
                        isFetching = false,
                    ))
                }
                showDialog = false
            },
            onConfirm = { input ->
                when(uiState.value.currentDialogType){
                    DialogType.TWITTER_USER_INFO_DOWNLOAD ->{
                        if (uiState.value.info.first.isEmpty()) {
                            scope.launch {
                                viewModel.emitIntent(AdvancedPageUIIntent.GetSomeonesTwitterAccountInfo(input))
                            }
                        } else {
                            scope.launch {
                                viewModel.emitIntent(AdvancedPageUIIntent.OnConfirmTwitterDownloadMedia(uiState.value.info.third, uiState.value.info.first))
                                delay(200)
                                viewModel.emitState(uiState.value.copy(
                                    info = Triple("", "", "")
                                ))
                            }
                            showDialog = false
                        }
                    }
                    DialogType.LOFTER_AUTHOR_TAGS -> {
                        scope.launch {
                            viewModel.emitIntent(AdvancedPageUIIntent.GetLofterPicsByTags(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", "")
                            ))
                            showDialog = false
                        }
                    }
                    DialogType.KUAIKAN_ENTIRE -> {
                        scope.launch {
                            viewModel.emitIntent(AdvancedPageUIIntent.GetKuaikanEntireManga(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", "")
                            ))
                            showDialog = false
                        }

                    }
                    DialogType.NONE -> TODO()
                }
            }
        )
        Column(
            modifier = Modifier
                .paddingTop(SizeTokens.Level8)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Section(title = "Twitter") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    AdvancedPageTwitterButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = item.title,
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            when(index){
                                0 ->{showGetAllMyTwitterBookmarks = true}
                                1 ->{showGetMyTwitterLikes = true}
                                2 ->{showDevelopingAlert = true}
                                3 ->{
                                    scope.launch{
                                        viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.TWITTER_USER_INFO_DOWNLOAD))
                                    }
                                    dialogTitle = "Enter Twitter Username"
                                    dialogPlaceholder = "@ExampleUser"
                                    showDialog = true
                                }
                            }
                        }
                    }
                }
            }
            Section(title = "Lofter") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    AdvancedPageLofterButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = item.title,
                            icon = item.icon,
                            color = when (index) {
                                0 -> MaterialTheme.colorScheme.primaryContainer
                                1 -> if (eligibleToUseLofterGetByTags.value) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            when (index) {
                                0 -> {showDevelopingAlert = true}
                                1 -> {
                                    if (eligibleToUseLofterGetByTags.value) {
                                        scope.launch{
                                            viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.LOFTER_AUTHOR_TAGS))
                                        }
                                        dialogTitle = "Enter Lofter author's Homepage"
                                        dialogPlaceholder = "https://username.lofter.com/"
                                        showDialog = true
                                    } else {
                                        navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Section(title = "Kuaikan") {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    AdvancedPageKuaikanButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = item.title,
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            when (index) {
                                0 -> {
                                    scope.launch{
                                        viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.KUAIKAN_ENTIRE))
                                    }
                                    dialogTitle = "Enter Kuaikan Comic's URL"
                                    dialogPlaceholder = "kuaikanmanhua.com/web/topic/..."
                                    showDialog = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}