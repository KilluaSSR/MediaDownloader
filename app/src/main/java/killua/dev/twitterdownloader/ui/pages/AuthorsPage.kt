package killua.dev.twitterdownloader.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.PrepareRoutes
import killua.dev.base.ui.UserPageLofterButtons
import killua.dev.base.ui.UserPageTwitterButtons
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.ui.ViewModels.AuthorPageUIIntent
import killua.dev.twitterdownloader.ui.ViewModels.AuthorPageViewModel
import killua.dev.twitterdownloader.ui.components.MainScaffold
import killua.dev.twitterdownloader.ui.components.AuthorsPageTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthorsPage(){
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val viewModel: AuthorPageViewModel = hiltViewModel()
    viewModel.uiState.collectAsStateWithLifecycle()
    val eligibleToUseLofterGetByTags = viewModel.lofterGetByTagsEligibility.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(AuthorPageUIIntent.OnEntry(context))
    }
    MainScaffold (
        topBar = {
            AuthorsPageTopAppBar(navController)
        },

    ){
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
                    UserPageTwitterButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = item.title,
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            navController.navigateSingle(item.route)
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
                    UserPageLofterButtons.forEachIndexed { index, item ->
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