package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import killua.dev.core.MainPageDropdownMenuButtons
import killua.dev.core.utils.navigateSingle

@Composable
fun MainScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
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
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPageTopBar(navController: NavHostController){
    var isMenuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text("Twitter Downloader") },
        actions = {
            IconButton(
                onClick = {isMenuExpanded = true}
            ) {
                Icon(imageVector = Icons.Default.ExpandMore,null)
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = {isMenuExpanded = false},
                        modifier = Modifier
                    ) {
                        MainPageDropdownMenuButtons.forEach{
                            DropdownMenuItem(
                                text = { Text(it.title) },
                                onClick = {
                                    navController.navigateSingle(it.route)
                                }
                            )
                        }
                    }
            }
        }
    )
}