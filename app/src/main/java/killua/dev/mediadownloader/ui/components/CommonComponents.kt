package killua.dev.mediadownloader.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotInterested
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import killua.dev.mediadownloader.Model.AppIcon
import killua.dev.mediadownloader.Model.AvailablePlatforms
import killua.dev.mediadownloader.Model.platformName
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.datastore.writeTheme
import killua.dev.mediadownloader.ui.PrepareRoutes
import killua.dev.mediadownloader.ui.components.common.CancellableAlert
import killua.dev.mediadownloader.ui.theme.ThemeMode
import killua.dev.mediadownloader.ui.theme.getThemeModeName
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.navigateSingle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotLoggedInAlert(
    platform: AvailablePlatforms,
    navController: NavHostController,
    onDismiss: ()-> Unit
){
    val context = LocalContext.current
    CancellableAlert(
        "${context.getString(platformName[platform]!!)} ${stringResource(R.string.not_logged_in)}",
        stringResource(R.string.cookie_necessary, context.getString(platformName[platform]!!)),
        icon = {
            AppIcon(platform)
        },
        onDismiss = onDismiss,
    ) {
        when(platform){
            AvailablePlatforms.Twitter -> {
                navController.navigateSingle(PrepareRoutes.TwitterPreparePage.route)
            }
            AvailablePlatforms.Lofter -> {
                navController.navigateSingle(PrepareRoutes.LofterPreparePage.route)
            }

            AvailablePlatforms.Pixiv -> {
                navController.navigateSingle(PrepareRoutes.PixivPreparePage.route)
            }

            AvailablePlatforms.Kuaikan -> {
                navController.navigateSingle(PrepareRoutes.KuaikanPreparePage.route)
            }

            AvailablePlatforms.MissEvan -> {
                navController.navigateSingle(PrepareRoutes.MissEvavnPreparePage.route)
            }
        }
    }
}

@Composable
fun EmptyIndicator(
    @StringRes textRes: Int
){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Rounded.NotInterested,
                contentDescription = null,
                modifier = Modifier
                    .alpha(0.3f)
                    .size(SizeTokens.Level72)
            )
            Spacer(modifier = Modifier.size(SizeTokens.Level16))
            Text(
                text = stringResource(textRes),
                modifier = Modifier.alpha(0.3f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimatedDropdownMenu(
    expanded: Boolean,
    selectedIndex: Int,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    LocalDensity.current
    48.dp
    val items = remember { mutableStateListOf<Int>() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(expanded) {
        if (expanded) {
            // 从选中项开始展开
            items.clear()
            items.add(selectedIndex)
            // 向上添加项
            for (i in selectedIndex - 1 downTo 0) {
                delay(50)
                items.add(i)
            }
            // 向下添加项
            for (i in selectedIndex + 1..2) {
                delay(50)
                items.add(i)
            }
        }
    }

    if (expanded) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            offset = offset
        ) {
            Column {
                ThemeMode.entries.forEachIndexed { index, theme ->
                    AnimatedVisibility(
                        visible = items.contains(index),
                        enter = fadeIn() + expandVertically(
                            expandFrom = if (index < selectedIndex) Alignment.Bottom else Alignment.Top
                        )
                    ) {
                        DropdownMenuItem(
                            text = { Text(getThemeModeName(theme)) },
                            onClick = {
                                scope.launch {
                                    context.writeTheme(theme.name)
                                    onDismissRequest()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}