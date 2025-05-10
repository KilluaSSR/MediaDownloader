package killua.dev.mediadownloader.ui.pages

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.Model.DownloadPageCommands
import killua.dev.mediadownloader.Model.DownloadPageDestinations
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.FilterContent
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.CancelAll
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.CancelDownloadList
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.FilterDownloads
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.GoTo
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.PauseDownload
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.RetryAll
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.RetryDownload
import killua.dev.mediadownloader.ui.ViewModels.DownloadListPageUIIntent.UpdateFilterOptions
import killua.dev.mediadownloader.ui.ViewModels.DownloadListViewModel
import killua.dev.mediadownloader.ui.components.DownloadItemCard
import killua.dev.mediadownloader.ui.components.EmptyIndicator
import killua.dev.mediadownloader.ui.components.Loading
import killua.dev.mediadownloader.ui.components.MainScaffold
import killua.dev.mediadownloader.ui.components.common.BottomSheet
import killua.dev.mediadownloader.ui.components.common.DownloadPageTopAppBar
import killua.dev.mediadownloader.ui.components.common.FileNotFountAlert
import killua.dev.mediadownloader.ui.components.common.paddingBottom
import killua.dev.mediadownloader.ui.components.common.paddingHorizontal
import killua.dev.mediadownloader.ui.tokens.SizeTokens

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun DownloadListPage() {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel: DownloadListViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    MainScaffold(
        topBar = { DownloadPageTopAppBar(
            navController,
            retryAllOnClick = {
                viewModel.launchOnIO {
                    viewModel.emitIntent(RetryAll)
                }
            },
            cancelOnClick = {
                viewModel.launchOnIO {
                    viewModel.emitIntent(CancelAll)
            }},
            showMoreOnClick = {
                showBottomSheet = true
            }
        ) },
        snackbarHostState = viewModel.snackbarHostState
    ) {
        var showFileNotFoundAlert by remember { mutableStateOf(false) }
        if(showBottomSheet){
            BottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                FilterContent(
                    availableAuthors = uiState.value.availableAuthors,
                    currentFilter = uiState.value.filterOptions,
                    onFilterChange = { newFilter ->
                        viewModel.launchOnIO {
                            viewModel.emitIntent(UpdateFilterOptions(newFilter))
                        }
                    }
                )
            }
        }
        if(showFileNotFoundAlert){
            FileNotFountAlert { showFileNotFoundAlert = false }
        }
        Column(modifier = Modifier.fillMaxSize()) {
            var enabled by remember { mutableStateOf(true) }
            val options = remember { DownloadPageDestinations.entries }
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .paddingHorizontal(SizeTokens.Level16)
                    .paddingBottom(SizeTokens.Level16)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        enabled = enabled,
                        onClick = {
                            viewModel.launchOnIO {
                                enabled = false
                                viewModel.emitState(uiState.value.copy(
                                    optionIndex = index,
                                    optionsType = when (index) {
                                        0 -> DownloadPageDestinations.All
                                        1 -> DownloadPageDestinations.Downloading
                                        2 -> DownloadPageDestinations.Completed
                                        3 -> DownloadPageDestinations.Failed
                                        else -> throw IllegalArgumentException("Invalid index: $index")
                                    }
                                ))
                                viewModel.emitIntent(
                                    when (index) {
                                        0 -> FilterDownloads(DownloadPageDestinations.All)
                                        1 -> FilterDownloads(DownloadPageDestinations.Downloading)
                                        2 -> FilterDownloads(DownloadPageDestinations.Completed)
                                        3 -> FilterDownloads(DownloadPageDestinations.Failed)
                                        else -> throw IllegalArgumentException("Invalid index: $index")
                                    }
                                )
                                enabled = true
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        selected = (index == uiState.value.optionIndex)
                    ) {
                        Text(
                            when(index){
                            0 -> stringResource(R.string.all)
                            1 -> stringResource(R.string.downloading)
                            2 -> stringResource(R.string.completed)
                            3 -> stringResource(R.string.failed)
                            else -> stringResource(R.string.all)
                            }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            if(uiState.value.isLoading) {
                Loading()
            }
            AnimatedContent(
                targetState = uiState.value.optionsType,
                transitionSpec = {
                    val direction = if (targetState.ordinal > initialState.ordinal) {
                        AnimatedContentTransitionScope.SlideDirection.Left
                    } else {
                        AnimatedContentTransitionScope.SlideDirection.Right
                    }
                    slideIntoContainer(direction) + fadeIn() togetherWith
                            slideOutOfContainer(direction) + fadeOut()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.value.downloads.isEmpty()) {
                    EmptyIndicator(R.string.nothing_to_show)
                } else{
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
                        ) {
                            items(uiState.value.downloads) { download ->
                                DownloadItemCard(
                                    item = download,
                                    onCommand = { item, cmd ->
                                        when (cmd) {
                                            DownloadPageCommands.Resume,
                                            DownloadPageCommands.Retry -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        RetryDownload(item.id)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.Pause -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        PauseDownload(item.id)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.Cancel -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        CancelDownloadList(item.id)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.Delete -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        CancelDownloadList(item.id)  //Same
                                                    )
                                                }
                                            }

                                            DownloadPageCommands.GoTo -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        GoTo(item.id,context)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.FilterHisAll -> TODO()
                                        }
                                    },
                                    thumbnailCache = uiState.value.thumbnailCache,
                                    modifier = Modifier.animateItemPlacement(),
                                    fileNotFoundClick = { showFileNotFoundAlert = true },
                                    downloadProgress = uiState.value.downloadProgress
                                )
                            }
                        }
                    }
                }
            }
        }
}
