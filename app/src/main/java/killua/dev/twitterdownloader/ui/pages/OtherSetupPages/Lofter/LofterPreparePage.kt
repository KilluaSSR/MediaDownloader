package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import killua.dev.base.Model.AvailablePlatforms
import killua.dev.base.states.CurrentState
import killua.dev.base.ui.CookiesRoutes
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.AppIcon
import killua.dev.base.ui.components.HeadlineMediumText
import killua.dev.base.ui.components.PermissionButton
import killua.dev.base.ui.components.Section
import killua.dev.base.ui.components.paddingTop
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.setup.SetupRoutes
import killua.dev.setup.ui.components.SetupScaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import killua.dev.base.datastore.readLofterEndTime
import killua.dev.base.datastore.readLofterStartTime
import killua.dev.base.datastore.writeLofterEndTime
import killua.dev.base.datastore.writeLofterStartTime
import killua.dev.base.ui.PrepareRoutes
import killua.dev.base.ui.components.DateRangePickerModal
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
    val viewModel: LofterPreparePageViewModel = viewModel()
    val navController = LocalNavController.current!!
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    val dateSelectedState = viewModel.dateSelectedState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val tagsAddedState = viewModel.tagsAddedState.collectAsStateWithLifecycle()
    val eligibility = viewModel.eligibility.collectAsStateWithLifecycle()
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) {
        viewModel.emitIntentOnIO(LofterPreparePageUIIntent.OnEntry(context))
        startDate = context.readLofterStartTime().first()
        endDate = context.readLofterEndTime().first()
    }

    SetupScaffold(
        actions = {
            Button(
                enabled = eligibility.value,
                onClick = {
                    viewModel.launchOnIO {
                        navController.popBackStack()
                    }
                }
            ) {
                Text(text = "Continue")
            }
        }
    ) {
        val dateString by remember(startDate, endDate) {
            mutableStateOf(
                if (startDate != null && endDate != null) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    "From ${dateFormat.format(Date(startDate!!))} to ${dateFormat.format(Date(endDate!!))}"
                } else {
                    "Please select a date range"
                }
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
                        }
                        //viewModel.emitIntentOnIO(LofterPreparePageUIIntent.OnDateChanged)
                    }
                },
                onDismiss = { showDatePicker = false }
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Column(
                modifier = Modifier
                    .paddingTop(SizeTokens.Level100),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIcon(AvailablePlatforms.Lofter)
                HeadlineMediumText(modifier = Modifier.paddingTop(SizeTokens.Level12), text = "Lofter Configurations")
            }

            Section(title = "Log in to your Lofter account") {
                PermissionButton(
                    title = "Log in",
                    description = "Your Lofter's cookie is necessary when downloading pictures from it.",
                    state = loginState.value,
                    onClick = {
                        if(loginState.value != CurrentState.Success){
                            navController.navigateSingle(CookiesRoutes.LofterCookiesBrowser.route)
                        }
                    },
                    color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                )
            }

            Section(title = "Date picker") {
                PermissionButton(
                    title = "Range",
                    description = dateString,
                    state = dateSelectedState.value,
                    onClick = {
                        showDatePicker = true
                    },
                    color = if (dateSelectedState.value == CurrentState.Idle || dateSelectedState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                )
            }

            Section(title = "Your favorite Tags") {
                PermissionButton(
                    title = "Edit tags",
                    description = "Images including selected tags will be downloaded.",
                    state = tagsAddedState.value,
                    onClick = {
                        navController.navigateSingle(PrepareRoutes.LofterPrepareTagsPage.route)
                    },
                    color = if (loginState.value == CurrentState.Idle || loginState.value == CurrentState.Error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                )

            }
        }
    }
}
