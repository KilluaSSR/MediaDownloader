package killua.dev.twitterdownloader.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import killua.dev.twitterdownloader.ui.tokens.SizeTokens

@Composable
fun SetupScaffold(
    topBar: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable LazyItemScope.() -> Unit
){
    Scaffold(
        topBar = topBar,
        containerColor = MaterialTheme.colorScheme.surface
    ){innerPadding ->
        Column{
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ){
                LazyColumn(modifier = Modifier.fillMaxSize()){
                    item{
                        InnerTopPadding(innerPadding)
                    }
                    item{
                        content()
                    }
                    item{
                        InnerBottomPadding(innerPadding)
                    }
                }
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(SizeTokens.Level16),
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level12, Alignment.End)
            ){
                actions()
            }
        }
    }

}