package killua.dev.mediadownloader.ui.pages

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
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.ViewModels.UserInfoPageViewModel
import killua.dev.mediadownloader.ui.components.UserInfoPageTopAppBar
import killua.dev.mediadownloader.ui.components.common.MainScaffold
import killua.dev.mediadownloader.ui.components.common.paddingTop
import killua.dev.mediadownloader.ui.getRandomColors
import killua.dev.mediadownloader.ui.tokens.SizeTokens

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
            UserInfoPageTopAppBar(navController)
        },
        snackbarHostState = viewmodel.snackbarHostState
    ) {
        Column(
            modifier = Modifier
                .paddingTop(SizeTokens.Level8)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {

        }
    }
}
