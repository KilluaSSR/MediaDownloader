package killua.dev.twitterdownloader.ui.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.MainScaffold
import killua.dev.base.ui.components.MainTopBar
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.getRandomColors
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.twitterdownloader.ui.ViewModels.UserInfoPageViewModel
import killua.dev.twitterdownloader.ui.components.FavouriteCard

@Composable
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
fun UserInfoPage() {
    getRandomColors()
    val navController = LocalNavController.current!!
    val viewmodel : UserInfoPageViewModel = hiltViewModel()
    viewmodel.uiState.collectAsStateWithLifecycle()
    LocalContext.current
    rememberCoroutineScope()
    MainScaffold(
        topBar = {
            MainTopBar(navController, {})
        },
        snackbarHostState = viewmodel.snackbarHostState
    ) {
        Column(
            modifier = Modifier
                .paddingTop(SizeTokens.Level8)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Section(title = "Your favourite user") {
                FavouriteCard("KilluaSSR", "KilluaSSR", 520, true){}
            }
            Section(title = "Total downloads") {
                FavouriteCard("KilluaSSR", "KilluaSSR", 520, true){}
            }
            Section(title = "Download Time") {
                FavouriteCard("KilluaSSR", "KilluaSSR", 520, true){}
            }
            Section(title = "Overview") {
                FavouriteCard("KilluaSSR", "KilluaSSR", 520, true){}
            }
        }
    }
}
