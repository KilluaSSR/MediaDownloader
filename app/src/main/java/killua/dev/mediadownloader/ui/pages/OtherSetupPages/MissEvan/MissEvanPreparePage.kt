package killua.dev.mediadownloader.ui.pages.OtherSetupPages.MissEvan

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.datastore.writeMissEvanToken
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.components.ConfigurationPage
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageUIIntent
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageViewModel
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissEvanPreparePage() {
    val viewModel: PreparePageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(PreparePageUIIntent.OnEntryMissEvan)
    }

    ConfigurationPage(
        platform = AvailablePlatforms.MissEvan,
        loginStateFlow = viewModel.missevanLoginState,
        eligibilityFlow = viewModel.missEvanEligibility,
        onLogin = {
            navController.navigateSingle(CookiesRoutes.MissEvanCookiesBrowser.route)
        },
        onLogout = {
            scope.launch {
                context.writeMissEvanToken("")
                viewModel.emitIntent(PreparePageUIIntent.OnMissEvanLoggedOut)
            }
        },
        onContinue = {
            navController.popBackStack()
        }
    )
}