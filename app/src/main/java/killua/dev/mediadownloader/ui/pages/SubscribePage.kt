package killua.dev.mediadownloader.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.ViewModels.SubscribePageUIIntent
import killua.dev.mediadownloader.ui.ViewModels.SubscribePageViewModel
import killua.dev.mediadownloader.ui.components.Loading
import killua.dev.mediadownloader.ui.components.MainPageBottomSheet
import killua.dev.mediadownloader.ui.components.MainScaffold
import killua.dev.mediadownloader.ui.components.common.SubscribePageTopAppBar
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscribePage() {
    val navController = LocalNavController.current!!
    LocalContext.current
    rememberModalBottomSheetState()
    val viewModel: SubscribePageViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    MainScaffold(
        topBar = {
            SubscribePageTopAppBar(
                navController,
                selectAllOnClick = {
                    scope.launch {
                        viewModel.emitIntent(SubscribePageUIIntent.SubscribeAll)
                    }
                },
                cancelAllOnClick = {
                    scope.launch {
                        viewModel.emitIntent(SubscribePageUIIntent.CancelAll)
                    }
                },
            ) {
                showBottomSheet = true
            }
        },
        snackbarHostState = viewModel.snackbarHostState
    ) {
        if(showBottomSheet){

        }

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = uiState.filterQuery,
                onValueChange = { query ->
                    scope.launch {
                        viewModel.emitIntent(SubscribePageUIIntent.FilterAuthors(query))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SizeTokens.Level16),
                label = { Text(stringResource(R.string.search_authors)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                },
                trailingIcon = {
                    if (uiState.filterQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            scope.launch {
                                viewModel.emitIntent(SubscribePageUIIntent.FilterAuthors(""))
                            }
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                        }
                    }
                }
            )

            when {
                uiState.isLoading -> {
                    Loading()
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.loading_failed, uiState.error),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(SizeTokens.Level16))
                            Button(onClick = {
                                scope.launch {
                                    viewModel.emitIntent(SubscribePageUIIntent.LoadSubscriptions)
                                }
                            }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                uiState.filteredAuthors.isEmpty() && uiState.filterQuery.isNotEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_matching_authors, uiState.filterQuery))
                    }
                }
                uiState.filteredAuthors.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.no_twitter_authors))
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(uiState.filteredAuthors) { _, authorPair ->
                            val author = authorPair.first
                            val isSubscribed = authorPair.second
                            AuthorSubscriptionItem(
                                author = author,
                                isSubscribed = isSubscribed,
                                onSubscriptionChanged = { subscribed ->
                                    scope.launch {
                                        viewModel.emitIntent(SubscribePageUIIntent.ToggleSubscription(author, subscribed))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorSubscriptionItem(
    author: String,
    isSubscribed: Boolean,
    onSubscriptionChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level8),
        elevation = CardDefaults.cardElevation(defaultElevation = SizeTokens.Level1),
        colors = CardDefaults.cardColors(
            if (isSubscribed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SizeTokens.Level16),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isSubscribed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(SizeTokens.Level24)
                )
                Spacer(modifier = Modifier.width(SizeTokens.Level16))
                Column {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSubscribed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )
                    Text(
                        text = if (isSubscribed)
                            stringResource(R.string.subscribed)
                        else
                            stringResource(R.string.not_subscribed),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSubscribed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            Switch(
                checked = isSubscribed,
                onCheckedChange = onSubscriptionChanged,
                thumbContent = if (isSubscribed) {
                    {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    }
}