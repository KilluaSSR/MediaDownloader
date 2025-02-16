package killua.dev.mediadownloader.ui.components.common

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import killua.dev.mediadownloader.ui.tokens.SizeTokens

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
fun SettingsScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    snackbarHostState: SnackbarHostState? = null,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (BoxScope.(innerPadding: PaddingValues) -> Unit)
){
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SecondaryLargeTopBar(
                scrollBehavior = scrollBehavior,
                title = title,
                actions = actions
            )
        },
        snackbarHost = {
            if (snackbarHostState != null) {
                SnackbarHost(
                    modifier = Modifier.paddingBottom(SizeTokens.Level24 + SizeTokens.Level4),
                    hostState = snackbarHostState,
                )
            }
        },
    ) { innerPadding ->
        Column {
            InnerTopPadding(innerPadding = innerPadding)

            Box(modifier = Modifier.weight(1f), content = { content(this, innerPadding) })
        }
    }
}

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun InputSetupScaffold(
    scrollBehavior: TopAppBarScrollBehavior, title: String,
    snackbarHostState: SnackbarHostState,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (BoxScope.(innerPadding: PaddingValues) -> Unit)
) {
    var _innerPadding by remember { mutableStateOf(PaddingValues(SizeTokens.Level0)) }
    var bottomBarSize by remember { mutableStateOf(IntSize.Zero) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SecondaryLargeTopBar(
                scrollBehavior = scrollBehavior,
                title = title,
            )
        },
        snackbarHost = {
            with(LocalDensity.current) {
                SnackbarHost(
                    modifier = Modifier
                        .consumeWindowInsets(_innerPadding)
                        .imePadding()
                        .paddingBottom(bottomBarSize.height.toDp() + SizeTokens.Level24 + SizeTokens.Level4),
                    hostState = snackbarHostState,
                )
            }
        },
    ) { innerPadding ->
        _innerPadding = innerPadding
        Column(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        InnerTopPadding(innerPadding = innerPadding)
                    }
                    item {
                        content(innerPadding)
                    }

                    item {
                        InnerBottomPadding(innerPadding = innerPadding)
                    }
                }
            }

            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SizeTokens.Level16)
                    .onSizeChanged { bottomBarSize = it },
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level12, Alignment.End),
            ) {
                actions()
            }

            InnerBottomPadding(innerPadding = innerPadding)
        }
    }
}

