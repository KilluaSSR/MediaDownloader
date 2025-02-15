package killua.dev.mediadownloader.ui.pages.OtherSetupPages.Kuaikan

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.datastore.writeKuaikanPassToken
import killua.dev.base.ui.CookiesRoutes
import killua.dev.base.ui.LocalNavController
import killua.dev.base.utils.navigateSingle
import killua.dev.mediadownloader.ui.components.ConfigurationPage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageUIIntent
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageViewModel
import kotlinx.coroutines.launch


@Composable
fun KuaikanPreparePage() {
    val viewModel: PreparePageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(PreparePageUIIntent.OnEntryKuaikan(context))
    }

    ConfigurationPage(
        platform = AvailablePlatforms.Kuaikan,
        loginStateFlow = viewModel.kuaikanLoginState,
        eligibilityFlow = viewModel.kuaikanEligibility,
        onLogin = {
            navController.navigateSingle(CookiesRoutes.KuaikanCookiesBrowser.route)
        },
        onLogout = {
            scope.launch {
                context.writeKuaikanPassToken("")
                viewModel.emitIntent(PreparePageUIIntent.OnKuaikanLoggedOut)
            }
        },
        onContinue = {
            navController.popBackStack()
        }
    )
}