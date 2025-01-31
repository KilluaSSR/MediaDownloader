package killua.dev.twitterdownloader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.BodyMediumText
import killua.dev.base.ui.components.BottomSheet
import killua.dev.base.ui.components.BottomSheetItem
import killua.dev.base.ui.components.OverviewCard
import killua.dev.base.ui.components.TitleLargeText
import killua.dev.base.ui.components.TopBar
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.MainPageMenuButtons
import killua.dev.twitterdownloader.MainRoutes

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
fun InputDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val focusRequester =
        remember { FocusRequester() }
    LaunchedEffect(showDialog) {
        if (!showDialog) {
            inputText = ""
        }
    }
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(SizeTokens.Level8),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(SizeTokens.Level16)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Twitter URL here",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.Companion.height(
                            SizeTokens.Level16
                        )
                    )

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        placeholder = { Text("https://x.com/...") },
                        singleLine = true
                    )

                    Spacer(
                        modifier = Modifier.Companion.height(
                            SizeTokens.Level24
                        )
                    )

                    Row(
                        modifier = Modifier.Companion.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            inputText = ""
                            onDismiss()
                        }) {
                            Text("Cancel")
                        }

                        Spacer(
                            modifier = Modifier.Companion.width(
                                SizeTokens.Level8
                            )
                        )

                        Button(
                            onClick = {
                                onConfirm(inputText)
                                onDismiss()
                            }
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageBottomSheet(onDismiss: () -> Unit,sheetState: SheetState, showDevelopingAlert: ()-> Unit){
    val navController = LocalNavController.current!!
    BottomSheet(onDismiss, sheetState) {
        MainPageMenuButtons.forEach { item->
            BottomSheetItem(item.icon,item.title) {
                if(item.route == MainRoutes.SettingPage.route){
                    navController.navigateSingle(item.route)
                }else{
                    showDevelopingAlert()
                }

            }
        }

    }
}