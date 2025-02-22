package killua.dev.mediadownloader.ui.components.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.utils.maybePopBackStack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(navController: NavHostController, showMoreOnClick : ()-> Unit ) {
    TopBar(navController, "Media Downloader", enableNavIcon = false,
        showMoreOnClick = showMoreOnClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController, title: String, enableNavIcon: Boolean = true, extraIcons: @Composable ()-> Unit = {}, showMoreIcon: Boolean = true, showMoreOnClick: ()-> Unit) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if(enableNavIcon){
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            extraIcons()
            if(showMoreIcon){
                IconButton(
                    onClick = showMoreOnClick
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        null
                    )
                }
            }
        }
    )
}

@ExperimentalMaterial3Api
@Composable
fun SecondaryLargeTopBar(
    scrollBehavior: TopAppBarScrollBehavior?,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    val navController = killua.dev.mediadownloader.ui.LocalNavController.current!!
    LargeTopAppBar(
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArrowBackButton {
                if (onBackClick != null) onBackClick.invoke()
                else navController.maybePopBackStack()
            }
        },
        actions = actions,
    )
}

@Composable
fun DownloadPageTopAppBar(
    navController: NavHostController,
    retryAllOnClick: () -> Unit,
    cancelOnClick: () -> Unit,
    showMoreOnClick: () -> Unit
) {
    TopBar(navController, stringResource(R.string.download_list), extraIcons = {
        IconButton(
            onClick = retryAllOnClick
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                "Retry all failed downloads"
            )
        }
        IconButton(
            onClick = cancelOnClick
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                "Cancel all active downloads"
            )
        }
    },
        showMoreOnClick = showMoreOnClick
    )
}