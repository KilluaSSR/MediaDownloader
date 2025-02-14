package killua.dev.setup.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import killua.dev.base.ui.components.InnerBottomPadding
import killua.dev.base.ui.components.InnerTopPadding
import killua.dev.base.ui.tokens.SizeTokens

@Composable
fun SetupScaffold(
    topBar: @Composable () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable LazyItemScope.() -> Unit
) {
    Scaffold(
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.surface,
        snackbarHost = {
            if (snackbarHostState != null)
                SnackbarHost(hostState = snackbarHostState)
        }
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
            Row(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(SizeTokens.Level16),
                horizontalArrangement = Arrangement.spacedBy(
                    SizeTokens.Level12,
                    Alignment.Companion.End
                )
            ) {
                actions()
            }
        }
    }

}