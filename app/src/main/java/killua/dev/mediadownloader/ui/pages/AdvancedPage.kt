package killua.dev.mediadownloader.ui.pages

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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.ClearAllChapters
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.ConfirmChapterSelection
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.DismissChapterSelection
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetKuaikanEntireManga
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetLofterPicsByTags
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetMissEvanEntireDrama
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetMyTwitterBookmark
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetMyTwitterLiked
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetPixivEntireNovel
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.GetSomeonesTwitterAccountInfo
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.OnConfirmTwitterDownloadMedia
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.OnEntry
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.SelectAllChapters
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageUIIntent.ToggleChapter
import killua.dev.mediadownloader.ui.ViewModels.AdvancedPageViewModel
import killua.dev.mediadownloader.ui.ViewModels.DialogType
import killua.dev.mediadownloader.ui.components.AdvancedMissEvanButtons
import killua.dev.mediadownloader.ui.components.AdvancedPageKuaikanButtons
import killua.dev.mediadownloader.ui.components.AdvancedPageLofterButtons
import killua.dev.mediadownloader.ui.components.AdvancedPageTopAppBar
import killua.dev.mediadownloader.ui.components.AdvancedPageTwitterButtons
import killua.dev.mediadownloader.ui.components.AdvancedPixivButtons
import killua.dev.mediadownloader.ui.components.MainScaffold
import killua.dev.mediadownloader.ui.components.common.ActionsBotton
import killua.dev.mediadownloader.ui.components.common.AdvancedInputDialog
import killua.dev.mediadownloader.ui.components.common.CancellableAlert
import killua.dev.mediadownloader.ui.components.common.DevelopingAlert
import killua.dev.mediadownloader.ui.components.common.Section
import killua.dev.mediadownloader.ui.components.common.paddingTop
import killua.dev.mediadownloader.ui.pages.AdvancedPage.ChapterSelectionDialog
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedPage(){
    val navController = LocalNavController.current!!
    val viewModel: AdvancedPageViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var showDevelopingAlert by remember { mutableStateOf(false) }
    val eligibleToUseLofterGetByTags = viewModel.lofterGetByTagsEligibility.collectAsStateWithLifecycle()
    val eligibleToUseTwitterFunctions = viewModel.twitterEligibility.collectAsStateWithLifecycle()
    var dialogTitle by remember { mutableStateOf("") }
    var dialogPlaceholder by remember { mutableStateOf("") }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(OnEntry)
    }
    MainScaffold (
        topBar = {
            AdvancedPageTopAppBar(navController)
        },
        snackbarHostState = viewModel.snackbarHostState
    ){
        if (showDevelopingAlert) {
            DevelopingAlert(
                onDismiss = { showDevelopingAlert = false }
            )
        }
        var showGetAllMyTwitterBookmarks by remember { mutableStateOf(false) }
        if(showGetAllMyTwitterBookmarks){
            CancellableAlert(
                title = stringResource(R.string.get_bookmarks),
                mainText = stringResource(R.string.get_bookmarks_desc),
                onDismiss = {showGetAllMyTwitterBookmarks = false}
            ) {
                scope.launch{
                    viewModel.emitIntent(GetMyTwitterBookmark)
                }
            }
        }
        var showGetMyTwitterLikes by remember { mutableStateOf(false) }
        if(showGetMyTwitterLikes){
            CancellableAlert(
                title = stringResource(R.string.get_likes),
                mainText = stringResource(R.string.get_bookmarks_desc),
                onDismiss = {showGetMyTwitterLikes = false}
            ) {
                scope.launch{
                    viewModel.emitIntent(GetMyTwitterLiked)
                }
            }
        }
        if (uiState.value.showChapterSelection) {
            ChapterSelectionDialog(
                chapters = uiState.value.chapters,
                onToggle = { index ->
                    scope.launch {
                        viewModel.emitIntent(ToggleChapter(index))
                    }
                },
                onConfirm = {
                    scope.launch {
                        viewModel.emitIntent(ConfirmChapterSelection)
                    }
                },
                onDismiss = {
                    scope.launch {
                        viewModel.emitIntent(DismissChapterSelection)
                    }
                },
                onSelectAll = {
                    scope.launch {
                        viewModel.emitIntent(SelectAllChapters)
                    }
                },
                onClearAll = {
                    scope.launch {
                        viewModel.emitIntent(ClearAllChapters)
                    }
                }
            )
        }

        AdvancedInputDialog(
            title = dialogTitle,
            placeholder = dialogPlaceholder,
            showDialog = uiState.value.showDialog,
            loading = uiState.value.isFetching,
            userInfo = if (uiState.value.info.first.isNotEmpty())
                uiState.value.info else null,
            onDismiss = {
                scope.launch{
                    viewModel.emitState(uiState.value.copy(isFetching = false,info = Triple("", "", ""), showDialog = false))
                }
            },
            onCancel = {
                scope.launch {
                    viewModel.emitState(uiState.value.copy(
                        info = Triple("", "", ""),
                        isFetching = false,
                        showDialog = false
                    ))
                }
            },
            onConfirm = { input ->
                when(uiState.value.currentDialogType){
                    DialogType.TWITTER_USER_INFO_DOWNLOAD ->{
                        if (uiState.value.info.first.isEmpty()) {
                            scope.launch {
                                viewModel.emitIntent(GetSomeonesTwitterAccountInfo(input))
                            }
                        } else {
                            scope.launch {
                                viewModel.emitIntent(OnConfirmTwitterDownloadMedia(uiState.value.info.third, uiState.value.info.first))
                                delay(200)
                                viewModel.emitState(uiState.value.copy(
                                    info = Triple("", "", ""),
                                    showDialog = false
                                ))
                            }
                        }
                    }
                    DialogType.LOFTER_AUTHOR_TAGS -> {
                        scope.launch {
                            viewModel.emitIntent(GetLofterPicsByTags(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", ""),
                                showDialog = false
                            ))
                        }
                    }
                    DialogType.KUAIKAN_ENTIRE -> {
                        scope.launch {
                            viewModel.emitIntent(GetKuaikanEntireManga(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", "")
                            ))
                        }
                    }
                    DialogType.NONE -> {}
                    DialogType.PIXIV_ENTIRE_NOVEL -> {
                        scope.launch {
                            viewModel.emitIntent(GetPixivEntireNovel(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", "")
                            ))
                        }
                    }

                    DialogType.MissEvan -> {
                        scope.launch {
                            viewModel.emitIntent(GetMissEvanEntireDrama(input))
                            delay(200)
                            viewModel.emitState(uiState.value.copy(
                                info = Triple("", "", "")
                            ))
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
            Section(title = stringResource(R.string.twitter)) {
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
                            title = stringResource(item.titleRes),
                            icon = item.icon,
                            color = if(eligibleToUseTwitterFunctions.value){
                                MaterialTheme.colorScheme.primaryContainer
                            }else{
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ) {
                            when(index){
                                0 ->{
                                    if (eligibleToUseTwitterFunctions.value) {
                                        showGetAllMyTwitterBookmarks = true
                                    }else{
                                        navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
                                    }
                                }
                                1 ->{if (eligibleToUseTwitterFunctions.value) {
                                    showGetMyTwitterLikes = true
                                }else{
                                    navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
                                }}
                                2 ->{
                                    if(eligibleToUseTwitterFunctions.value){
                                        scope.launch{
                                            viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.TWITTER_USER_INFO_DOWNLOAD, showDialog = true))
                                        }
                                        dialogTitle = context.getString(R.string.enter_twitter_username)
                                        dialogPlaceholder = "@ExampleUser"
                                    }else{
                                        navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Section(title = stringResource(R.string.lofter)) {
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
                            title = stringResource(item.titleRes),
                            icon = item.icon,
                            color = when (index) {
                                0 -> if (eligibleToUseLofterGetByTags.value) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            when (index) {
                                0 -> {
                                    if (eligibleToUseLofterGetByTags.value) {
                                        scope.launch{
                                            viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.LOFTER_AUTHOR_TAGS, showDialog = true))
                                        }
                                        dialogTitle = context.getString(R.string.enter_lofter_author_homepage)
                                        dialogPlaceholder = "https://username.lofter.com/"
                                    } else {
                                        navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Section(title = stringResource(R.string.kuaikan)) {
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
                            title = stringResource(item.titleRes),
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            when (index) {
                                0 -> {
                                    scope.launch{
                                        viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.KUAIKAN_ENTIRE, showDialog = true))
                                    }
                                    dialogTitle = context.getString(R.string.enter_kuaikan_comic_url)
                                    dialogPlaceholder = "kuaikanmanhua.com/web/topic/..."
                                }
                            }
                        }
                    }
                }
            }
            Section(title = stringResource(R.string.pixiv)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    AdvancedPixivButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = stringResource(item.titleRes),
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            when (index) {
                                0 -> {
                                    scope.launch{
                                        viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.PIXIV_ENTIRE_NOVEL, showDialog = true))
                                    }
                                    dialogTitle = context.getString(R.string.enter_pixiv_entire_novel_url)
                                    dialogPlaceholder = "www.pixiv.net/novel/series/..."
                                }
                            }
                        }
                    }
                }
            }

            Section(title = stringResource(R.string.missevan)) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    maxItemsInEachRow = 2,
                ) {
                    AdvancedMissEvanButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = stringResource(item.titleRes),
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            when (index) {
                                0 -> {
                                    scope.launch{
                                        viewModel.emitState(uiState.value.copy(currentDialogType = DialogType.MissEvan, showDialog = true))
                                    }
                                    dialogTitle = context.getString(R.string.enter_missevan_drama_url)
                                    dialogPlaceholder = "https://www.missevan.com/mdrama/..."
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}