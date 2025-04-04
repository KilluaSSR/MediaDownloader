package killua.dev.mediadownloader.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.sp
import killua.dev.mediadownloader.Model.FavouriteUserInfo
import killua.dev.mediadownloader.Model.ReportOption
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.animations.AnimatedTextContainer
import killua.dev.mediadownloader.ui.components.common.BodyMediumText
import killua.dev.mediadownloader.ui.components.common.BottomSheet
import killua.dev.mediadownloader.ui.components.common.BottomSheetItem
import killua.dev.mediadownloader.ui.components.common.OverviewCard
import killua.dev.mediadownloader.ui.components.common.TitleLargeText
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.navigateSingle
import killua.dev.mediadownloader.utils.openGithubIssues
import killua.dev.mediadownloader.utils.openMail
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FavouriteCard(
    userInfo: FavouriteUserInfo,
    onClick: () -> Unit = {}
) {
    OverviewCard(
        title = stringResource(R.string.your_favourite),
        icon = Icons.Default.FavoriteBorder,
        colorContainer = MaterialTheme.colorScheme.primaryContainer,
        onColorContainer = MaterialTheme.colorScheme.onPrimaryContainer,
        content = {
            Column {
                AnimatedTextContainer(targetState = userInfo.displayName) { text ->
                    TitleLargeText(
                        text = text,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                AnimatedTextContainer(targetState = userInfo.description) { text ->
                    if (text.isNotEmpty()) {
                        BodyMediumText(
                            text = text,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        onClick = onClick
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageBottomSheet(onDismiss: () -> Unit,sheetState: SheetState, showDevelopingAlert: ()-> Unit, onShowReport: () -> Unit){
    val navController = LocalNavController.current!!
    BottomSheet(onDismiss, sheetState) {
        MainPageMenuButtons.forEach { item ->
            BottomSheetItem(item.icon, stringResource(item.titleRes)) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDialog(
    title: String,
    icon: (@Composable () -> Unit)?,
    onDismiss: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(ReportOption.Mail) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val inAnimDuration = 600
    val outAnimDuration = 450

    var isDialogVisible by remember { mutableStateOf(false) }

    val animationSpec = tween<Float>(
        durationMillis = if (isDialogVisible) inAnimDuration else outAnimDuration
    )

    val dialogAlpha by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    val dialogRotationX by animateFloatAsState(
        targetValue = if (isDialogVisible) 0f else -90f,
        animationSpec = animationSpec
    )

    val dialogScale by animateFloatAsState(
        targetValue = if (isDialogVisible) 1f else 0f,
        animationSpec = animationSpec
    )

    val dismissWithAnimation: () -> Unit = {
        scope.launch {
            isDialogVisible = false
            delay(outAnimDuration.toLong())
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        isDialogVisible = true
    }

    AlertDialog(
        onDismissRequest = { dismissWithAnimation() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .padding(SizeTokens.Level6)
            .alpha(dialogAlpha)
            .scale(dialogScale)
            .graphicsLayer { rotationX = dialogRotationX },
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
                modifier = Modifier.padding(horizontal = SizeTokens.Level2)
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
                        text = stringResource(R.string.mail),
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
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level2, Alignment.End)
            ) {
                TextButton(onClick = { dismissWithAnimation() }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                TextButton(onClick = {
                    when (selectedOption) {
                        ReportOption.Mail -> openMail(context)
                        ReportOption.GithubIssues -> openGithubIssues(context)
                    }
                    dismissWithAnimation()
                }) {
                    Text(
                        text = stringResource(R.string.confirm),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    )
}