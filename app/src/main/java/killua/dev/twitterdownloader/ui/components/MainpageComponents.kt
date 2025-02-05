package killua.dev.twitterdownloader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import killua.dev.base.Model.ReportOption
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.MainPageButtonsAction
import killua.dev.base.ui.components.BodyMediumText
import killua.dev.base.ui.components.BottomSheet
import killua.dev.base.ui.components.BottomSheetItem
import killua.dev.base.ui.components.OverviewCard
import killua.dev.base.ui.components.TitleLargeText
import killua.dev.base.ui.components.TopBar
import killua.dev.base.utils.navigateSingle
import killua.dev.base.ui.MainPageMenuButtons
import killua.dev.base.ui.MainRoutes
import killua.dev.base.ui.components.CommonInputDialog
import killua.dev.base.ui.components.InputDialogConfig
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.twitterdownloader.utils.openGithubIssues
import killua.dev.twitterdownloader.utils.openMail

@Composable
fun MainScaffold(
    topBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    content: @Composable () -> Unit) {
    Scaffold(
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            if (snackbarHostState != null)
                SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(navController: NavHostController, showMoreOnClick : ()-> Unit ) {
    TopBar(navController, "Twitter Downloader", enableNavIcon = false,
        showMoreOnClick = showMoreOnClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteCard(
    favouriteUser: String,
    favouriteUserScreenName: String,
    downloadCount: Int,
    downloaded: Boolean,
    onClick: () -> Unit
) {
    OverviewCard(
        title = "Your Favourite",
        icon = Icons.Default.FavoriteBorder,
        colorContainer = MaterialTheme.colorScheme.primaryContainer,
        onColorContainer = MaterialTheme.colorScheme.onPrimaryContainer,
        content = {
            Column {
                AnimatedVisibility(
                    visible = downloaded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TitleLargeText(
                        text = "$favouriteUser @$favouriteUserScreenName",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                AnimatedVisibility(
                    visible = downloaded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    BodyMediumText(
                        text = "You've downloaded his/her video $downloadCount times",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                AnimatedVisibility(
                    visible = !downloaded,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TitleLargeText(
                        text = "Nothing here",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        onClick = onClick
    )
}

@Composable
fun TwitterURLInputDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val config = InputDialogConfig(
        title = "Twitter URL here",
        placeholder = "https://x.com/...",
        singleLine = true
    )

    CommonInputDialog(
        showDialog = showDialog,
        config = config,
        onDismiss = onDismiss,
        onConfirm = onConfirm
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageBottomSheet(onDismiss: () -> Unit,sheetState: SheetState, showDevelopingAlert: ()-> Unit, onShowReport: () -> Unit){
    val navController = LocalNavController.current!!
    BottomSheet(onDismiss, sheetState) {
        MainPageMenuButtons.forEach { item ->
            BottomSheetItem(item.icon, item.title) {
                when (item.action) {
                    is MainPageButtonsAction.Navigate -> {
                        onDismiss()
                        navController.navigateSingle((item.action as MainPageButtonsAction.Navigate).route)
                    }
                    MainPageButtonsAction.ShowDialog -> {
                        onDismiss()
                        onShowReport()
                    }
                }
            }
        }

    }
}


@Composable
fun ReportDialog(
    title: String,
    icon: (@Composable () -> Unit)?,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(ReportOption.Mail) }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(SizeTokens.Level6),
        icon = icon,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(
                    start = SizeTokens.Level6,
                    end = SizeTokens.Level6,
                    top = SizeTokens.Level6
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(
                    horizontal = SizeTokens.Level2
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SizeTokens.Level72)
                        .selectable(
                            selected = selectedOption == ReportOption.Mail,
                            onClick = { selectedOption = ReportOption.Mail },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = SizeTokens.Level8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level4)
                ) {
                    RadioButton(
                        selected = selectedOption == ReportOption.Mail,
                        onClick = null
                    )
                    Text(
                        text = "Mail",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = SizeTokens.Level4),
                        fontSize = 18.sp
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SizeTokens.Level72)
                        .selectable(
                            selected = selectedOption == ReportOption.GithubIssues,
                            onClick = { selectedOption = ReportOption.GithubIssues },
                            role = Role.RadioButton
                        )
                        .padding(horizontal = SizeTokens.Level8),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level4)
                ) {
                    RadioButton(
                        selected = selectedOption == ReportOption.GithubIssues,
                        onClick = null
                    )
                    Text(
                        text = "Github Issues",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = SizeTokens.Level4),
                        fontSize = 18.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SizeTokens.Level6,
                        end = SizeTokens.Level6,
                        bottom = SizeTokens.Level6
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    SizeTokens.Level2, // 8dp
                    Alignment.End
                )
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                TextButton(onClick = {
                    when (selectedOption) {
                        ReportOption.Mail -> openMail(context)
                        ReportOption.GithubIssues -> openGithubIssues(context)
                    }
                    onDismiss()
                }) {
                    Text(
                        text = "Confirm",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun showReportDialog(){
    ReportDialog("Report", null, {})
}