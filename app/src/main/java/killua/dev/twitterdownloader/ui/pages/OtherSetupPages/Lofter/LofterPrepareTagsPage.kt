package killua.dev.twitterdownloader.ui.pages.OtherSetupPages.Lofter

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import killua.dev.base.ui.LocalNavController
import killua.dev.base.ui.components.InputSetupScaffold
import killua.dev.base.ui.components.SetupTextField
import killua.dev.base.ui.components.Title
import killua.dev.base.ui.components.paddingHorizontal
import killua.dev.base.ui.tokens.SizeTokens

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun LofterPrepareTagsPage(){
    val viewModel: LofterPrepareTagsPageViewModel = hiltViewModel()
    LocalContext.current
    LocalNavController.current!!
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    InputSetupScaffold(
        scrollBehavior = scrollBehavior,
        title = "Edit Tags",
        actions = {
            Button(
                onClick = {

                }
            ) {
                Text(text = "Continue")
            }
        },
        snackbarHostState = viewModel.snackbarHostState
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(SizeTokens.Level24)
        ) {
            Title(title = "Add tags here") {
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
                    label = "Tags"
                )
            }

            Title(title = "Tags") {
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
