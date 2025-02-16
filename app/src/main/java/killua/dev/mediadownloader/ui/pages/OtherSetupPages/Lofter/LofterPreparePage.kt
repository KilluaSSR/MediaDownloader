package killua.dev.mediadownloader.ui.pages.OtherSetupPages.Lofter

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.datastore.readLofterCookieExpiration
import killua.dev.mediadownloader.datastore.readLofterEndTime
import killua.dev.mediadownloader.datastore.readLofterStartTime
import killua.dev.mediadownloader.datastore.writeLofterEndTime
import killua.dev.mediadownloader.datastore.writeLofterLoginAuth
import killua.dev.mediadownloader.datastore.writeLofterLoginKey
import killua.dev.mediadownloader.datastore.writeLofterStartTime
import killua.dev.mediadownloader.ui.CookiesRoutes
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.SnackbarUIEffect
import killua.dev.mediadownloader.ui.components.ConfigurationPage
import killua.dev.mediadownloader.ui.components.common.ClickableConfigurationButton
import killua.dev.mediadownloader.ui.components.common.DateRangePickerModal
import killua.dev.mediadownloader.ui.components.common.Section
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageUIIntent
import killua.dev.mediadownloader.ui.pages.OtherSetupPages.PreparePageViewModel
import killua.dev.mediadownloader.utils.navigateSingle
import killua.dev.mediadownloader.utils.parseTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalFoundationApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LofterPreparePage() {
    val viewModel: PreparePageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showDatePicker by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    val snackbarHostState = viewModel.snackbarHostState
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(PreparePageUIIntent.OnEntryLofter)
        startDate = context.readLofterStartTime().first()
        endDate = context.readLofterEndTime().first()
    }

    LaunchedEffect(viewModel.lofterLoginState) {
        val unixTimeMS = context.readLofterCookieExpiration().first()
        if(unixTimeMS.isNotEmpty()){
            val time = parseTimestamp(unixTimeMS.toLong())
            viewModel.emitEffect(SnackbarUIEffect.ShowSnackbar("Your cookie will expire at $time due to platform limitations. Please log out and log back in at that time.","OK",true, SnackbarDuration.Short))
        }

    }

    val dateString by remember(startDate, endDate) {
        mutableStateOf(
            if (startDate != null && endDate != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                "From ${dateFormat.format(Date(startDate!!))} to ${dateFormat.format(Date(endDate!!))}"
            } else {
                "Please select a date range, and only the images within this range will be downloaded"
            }
        )
    }

    ConfigurationPage(
        platform = AvailablePlatforms.Lofter,
        loginStateFlow = viewModel.lofterLoginState,
        eligibilityFlow = viewModel.lofterEligibility,
        snackbarHostState = snackbarHostState,
        onLogin = {
            navController.navigateSingle(CookiesRoutes.LofterCookiesBrowser.route)
        },
        onLogout = {
            scope.launch {
                context.writeLofterLoginKey("")
                context.writeLofterLoginAuth("")
                viewModel.emitIntent(PreparePageUIIntent.OnLofterLoggedOut)
            }
        },
        onContinue = {
            navController.popBackStack()
        }
    ) {
        Section(title = "Date Range") {
            ClickableConfigurationButton(
                title = "Date Range for Image Download",
                description = dateString,
                state = viewModel.dateSelectedState.collectAsStateWithLifecycle().value,
                onClick = { showDatePicker = true },
                color = viewModel.dateSelectedState.collectAsStateWithLifecycle().value.backgroundColor
            )
        }

        Section(title = "Your favorite Tags") {
            ClickableConfigurationButton(
                title = "Edit tags",
                description = "Images including selected tags will be downloaded.",
                state = viewModel.tagsAddedState.collectAsStateWithLifecycle().value,
                onClick = {
                    navController.navigateSingle(PrepareRoutes.LofterPrepareTagsPage.route)
                },
                color = viewModel.tagsAddedState.collectAsStateWithLifecycle().value.backgroundColor
            )
        }

        if (showDatePicker) {
            DateRangePickerModal(
                onDateRangeSelected = { (start, end) ->
                    if (start != null && end != null) {
                        startDate = start
                        endDate = end
                        scope.launch(Dispatchers.IO) {
                            context.writeLofterStartTime(start)
                            context.writeLofterEndTime(end)
                            viewModel.emitIntent(PreparePageUIIntent.OnDateChanged)
                        }
                    }
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}