package killua.dev.twitterdownloader.ui.Pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import killua.dev.base.ui.components.ActionsBotton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.getRandomColors
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.MainPageButtons
import killua.dev.twitterdownloader.MainRoutes
import killua.dev.twitterdownloader.ui.FavouriteCard
import killua.dev.twitterdownloader.ui.InputDialog
import killua.dev.twitterdownloader.ui.MainPageTopBar
import killua.dev.twitterdownloader.ui.MainPageViewmodel
import killua.dev.twitterdownloader.ui.MainScaffold

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainPage(
    viewmodel: MainPageViewmodel = viewModel()
) {
    val randomColors = getRandomColors()
    val navController = rememberNavController()
    var showDialog by remember { mutableStateOf(false) }
    MainScaffold(
        topBar = {
            MainPageTopBar(navController)
        }
    ) {
        InputDialog(
            showDialog = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {

            }
        )
        Column(
            modifier = Modifier
                .paddingTop(SizeTokens.Level8)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Section(title = "Overview") {
                FavouriteCard("KilluaDev", "风过荒野", 1024) {}
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
                                navController.navigateSingle(item.route)
                            }
                        }
                    }
                }
            }

        }
    }
}

@Preview
@Composable
fun mainpagedemo() {
    MainPage()
}