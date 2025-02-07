package killua.dev.twitterdownloader.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.UserPageLofterButtons
import killua.dev.base.ui.UserPageTwitterButtons
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.twitterdownloader.ui.components.MainScaffold
import killua.dev.twitterdownloader.ui.components.UsersPageTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthorsPage(){
    val navController = LocalNavController.current!!
    LocalContext.current
    //val viewModel: DownloadedViewModel = hiltViewModel()
    //val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    MainScaffold (
        topBar = {
            UsersPageTopAppBar(navController)
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
                    UserPageLofterButtons.forEachIndexed { index, item ->
                        ActionsBotton(
                            modifier = Modifier.weight(1f),
                            enabled = true,
                            title = item.title,
                            icon = item.icon,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {

                        }
                    }
                }
            }
        }
    }
}