package killua.dev.mediadownloader.ui.pages.OtherSetupPages.Twitter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.datastore.writeApplicationUserAuth
import killua.dev.mediadownloader.datastore.writeApplicationUserCt0
import killua.dev.mediadownloader.datastore.writeApplicationUserID
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.components.ConfigurationPage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageUIIntent
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageViewModel
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.launch


@Composable
fun TwitterPreparePage() {
    val viewModel: PreparePageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(PreparePageUIIntent.OnEntryTwitter)
    }

    ConfigurationPage(
        platform = AvailablePlatforms.Twitter,
        loginStateFlow = viewModel.twitterLoginState,
        eligibilityFlow = viewModel.twitterEligibility,
        onLogin = {
            navController.navigateSingle(CookiesRoutes.TwitterCookiesBrowser.route)
        },
        onLogout = {
            scope.launch {
                context.writeApplicationUserAuth("")
                context.writeApplicationUserCt0("")
                context.writeApplicationUserID("")
                viewModel.emitIntent(PreparePageUIIntent.OnTwitterLoggedOut)
            }
        },
        onContinue = {
            navController.popBackStack()
        }
    )
}