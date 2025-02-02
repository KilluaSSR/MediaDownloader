package killua.dev.twitterdownloader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import killua.dev.base.ui.LocalNavController
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