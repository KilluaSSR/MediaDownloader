package killua.dev.twitterdownloader.ui

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import killua.dev.base.ui.components.BodyMediumText
import killua.dev.base.ui.components.InnerBottomPadding
import killua.dev.base.ui.components.InnerTopPadding
import killua.dev.base.ui.components.OverviewCard
import killua.dev.base.ui.components.TitleLargeText
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.navigateSingle
import killua.dev.twitterdownloader.MainPageDropdownMenuButtons

@Composable
fun MainScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column {
            Box(
                modifier = Modifier.Companion
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.Companion.fillMaxSize()) {
                    item {
                        InnerTopPadding(innerPadding)
                    }
                    item {
                        content()
                    }
                    item {
                        InnerBottomPadding(innerPadding)
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageTopBar(navController: NavHostController) {
    var isMenuExpanded by remember {
        mutableStateOf(
            false
        )
    }
    TopAppBar(
        title = { Text("Twitter Downloader") },
        actions = {
            IconButton(
                onClick = { isMenuExpanded = true }
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    null
                )
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.Companion
                ) {
                    MainPageDropdownMenuButtons.forEach {
                        DropdownMenuItem(
                            text = { Text(it.title) },
                            onClick = {
                                navController.navigateSingle(it.title)
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouriteCard(
    favouriteUser: String,
    favouriteUserScreenName: String,
    downloadCount: Int,
    onClick: () -> Unit
) {
    OverviewCard(
        title = "Your Favourite",
        icon = Icons.Default.FavoriteBorder,
        colorContainer = MaterialTheme.colorScheme.primaryContainer,
        onColorContainer = MaterialTheme.colorScheme.onPrimaryContainer,
        content = {
            TitleLargeText(
                text = "$favouriteUser @$favouriteUserScreenName",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            BodyMediumText(
                text = "You've downloaded his/her video $downloadCount times",
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) {
        onClick
    }
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