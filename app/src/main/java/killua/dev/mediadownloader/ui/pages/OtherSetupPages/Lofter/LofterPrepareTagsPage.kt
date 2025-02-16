package killua.dev.mediadownloader.ui.pages.OtherSetupPages.Lofter

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.LocalNavController
import killua.dev.mediadownloader.ui.components.common.InputSetupScaffold
import killua.dev.mediadownloader.ui.components.common.SetupTextField
import killua.dev.mediadownloader.ui.components.common.Title
import killua.dev.mediadownloader.ui.components.common.paddingHorizontal
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
fun LofterPrepareTagsPage(){
    val viewModel: LofterPrepareTagsPageViewModel = hiltViewModel()
    val navController = LocalNavController.current!!
    LocalContext.current
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        viewModel.emitIntent(LofterPrepareTagsPageUIIntents.OnEntry)
    }
    InputSetupScaffold(
        scrollBehavior = scrollBehavior,
        title = stringResource(R.string.edit_tags),
        actions = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                onClick = {
                    scope.launch{
                        viewModel.emitIntent(LofterPrepareTagsPageUIIntents.ClearAll)
                    }
                }
            ) {
                Text(text = stringResource(R.string.clear_all))
            }
            Button(
                onClick = {
                    scope.launch{
                        viewModel.emitIntent(LofterPrepareTagsPageUIIntents.SaveTags)
                    }
                    navController.popBackStack()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        snackbarHostState = viewModel.snackbarHostState
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Title(title = stringResource(R.string.add_tags_here)) {
                SetupTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingHorizontal(SizeTokens.Level24),
                    value = uiState.value.currentInput,
                    leadingIcon = Icons.Rounded.Tag,
                    onValueChange = { input ->
                        viewModel.emitIntentOnIO(LofterPrepareTagsPageUIIntents.UpdateInput(input))
                        if (input.contains(Regex("[,，\\s]"))) {
                            val tags = TagUtils.splitTags(input)
                            viewModel.emitIntentOnIO(LofterPrepareTagsPageUIIntents.AddBatchTags(tags))
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    label = stringResource(R.string.tags)
                )
            }

            Title(title = stringResource(R.string.tags)) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingHorizontal(SizeTokens.Level24),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
                ) {
                    uiState.value.tags.forEach { tag ->
                        DeletableInputChip(
                            text = tag,
                            onDismiss = {
                                viewModel.emitIntentOnIO(
                                    LofterPrepareTagsPageUIIntents.RemoveTag(tag)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeletableInputChip(
    text: String,
    onDismiss: () -> Unit,
) {
    InputChip(
        onClick = { },
        label = { Text(text) },
        selected = false,
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = "Delete",
                modifier = Modifier.clickable { onDismiss() },
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    )
}
