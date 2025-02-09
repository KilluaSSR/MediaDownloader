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
import killua.dev.base.ui.AdvancedPageLofterButtons
import killua.dev.base.ui.AdvancedPageTwitterButtons
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.PrepareRoutes
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.CancellableAlert
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.ui.ViewModels.AdvancedPageUIIntent
import killua.dev.twitterdownloader.ui.ViewModels.AdvancedPageViewModel
import killua.dev.twitterdownloader.ui.components.AdvancedPageTopAppBar
import killua.dev.twitterdownloader.ui.components.MainScaffold
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdvancedPage(){
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel: AdvancedPageViewModel = hiltViewModel()
    viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val eligibleToUseLofterGetByTags = viewModel.lofterGetByTagsEligibility.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(AdvancedPageUIIntent.OnEntry(context))
    }
    MainScaffold (
        topBar = {
            AdvancedPageTopAppBar(navController)
        },

    ){
        var showGetAllMyTwitterBookmarks by remember { mutableStateOf(false) }
        if(showGetAllMyTwitterBookmarks){
            CancellableAlert(
                title = "Get Bookmarks",
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
                title = "Get Likes",
                mainText = "Get the content I liked on Twitter.\nNote that if you have a lot of liked content, it may trigger a rate limit risk and could even lead to account suspension.",
                onDismiss = {showGetMyTwitterLikes = false}
            ) {
                scope.launch{
                    viewModel.emitIntent(AdvancedPageUIIntent.GetMyTwitterLiked)
                }
            }
        }
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
                                2 ->{navController.navigateSingle(item.route)}
                                3 ->{navController.navigateSingle(item.route)}
                            }
                        }
                    }
                }
            }
            Section(title = "Lofter (Disable your VPN before continuing)") {
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
                                0 -> navController.navigateSingle(item.route)
                                1 -> {
//                                    if (eligibleToUseLofterGetByTags.value) {
//                                        navController.navigateSingle(item.route)
//                                    } else {
//                                        navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
//                                    }
                                    navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}