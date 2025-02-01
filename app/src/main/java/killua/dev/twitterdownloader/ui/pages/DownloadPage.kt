package killua.dev.twitterdownloader.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotInterested
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.BottomSheet
import killua.dev.base.ui.components.paddingBottom
import killua.dev.base.ui.components.paddingHorizontal
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.Model.DownloadPageDestinations
import killua.dev.base.ui.components.FileNotFountAlert
import killua.dev.twitterdownloader.ui.DownloadItemCard
import killua.dev.twitterdownloader.ui.DownloadPageCommands
import killua.dev.twitterdownloader.ui.DownloadPageTopAppBar
import killua.dev.twitterdownloader.ui.FilterContent
import killua.dev.twitterdownloader.ui.MainScaffold
import killua.dev.twitterdownloader.ui.ViewModels.DownloadPageUIIntent
import killua.dev.twitterdownloader.ui.ViewModels.DownloadPageUIIntent.*
import killua.dev.twitterdownloader.ui.ViewModels.DownloadedViewModel

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun DownloadPage() {
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel: DownloadedViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
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
                            viewModel.emitIntent(DownloadPageUIIntent.UpdateFilterOptions(newFilter))
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
                        Text(label.name)
                    }
                }
            }
            if(uiState.value.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Loading...",
                            modifier = Modifier.alpha(0.3f)
                        )

                        Spacer(modifier = Modifier.size(SizeTokens.Level16))
                        LinearProgressIndicator(
                            modifier = Modifier
                                .size(width = SizeTokens.Level128, height = SizeTokens.Level8)
                        )
                    }
                }
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
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.NotInterested,
                                contentDescription = null,
                                modifier = Modifier
                                    .alpha(0.3f)
                                    .size(SizeTokens.Level72)
                            )
                            Spacer(modifier = Modifier.size(SizeTokens.Level16))
                            Text(
                                text = "Nothing to show",
                                modifier = Modifier.alpha(0.3f)
                            )
                        }
                    }
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
                                                        CancelDownload(item.id)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.Delete -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        CancelDownload(item.id)  //Same
                                                    )
                                                }
                                            }

                                            DownloadPageCommands.GoToTwitter -> {
                                                viewModel.launchOnIO {
                                                    viewModel.emitIntent(
                                                        GoToTwitter(item.id,context)
                                                    )
                                                }
                                            }
                                            DownloadPageCommands.FilterHisAll -> TODO()
                                        }
                                    },
                                    thumbnailCache = uiState.value.thumbnailCache,
                                    modifier = Modifier.animateItemPlacement(),
                                    fileNotFoundClick = { showFileNotFoundAlert = true }
                                )
                            }
                        }
                    }
                }
            }
        }
}
