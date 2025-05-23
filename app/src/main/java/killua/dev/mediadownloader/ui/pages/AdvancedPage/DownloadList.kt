package killua.dev.mediadownloader.ui.pages.AdvancedPage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import killua.dev.mediadownloader.Model.ChapterInfo
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.components.EmptyIndicator
import killua.dev.mediadownloader.ui.tokens.SizeTokens

@Composable
fun ExpandableFullScreenDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    if (!visible && !remember { mutableStateOf(visible) }.value) {
        return
    }

    // 创建过渡动画
    val transition = updateTransition(
        targetState = visible,
        label = "DialogTransition"
    )

    // 定义动画属性
    val alpha by transition.animateFloat(
        label = "DialogAlpha",
        transitionSpec = {
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        }
    ) { if (it) 1f else 0f }

    val padding by transition.animateDp(
        label = "DialogPadding",
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            )
        }
    ) { if (it) 0.dp else 48.dp }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(alpha),  // 应用透明度动画
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterSelectionDialog(
    chapters: List<Pair<ChapterInfo, Boolean>>,
    onToggle: (Int) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit
) {
    ExpandableFullScreenDialog(
        visible = true,
        onDismiss = onDismiss
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    title = { Text(stringResource(R.string.select_chapters)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "关闭")
                        }
                    },
                    actions = {
                        TextButton(onClick = onSelectAll) {
                            Text(stringResource(R.string.select_all),fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onClearAll) {
                            Text(stringResource(R.string.clear_all),fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = {
                            onConfirm()
                            onDismiss()
                        }) {
                            Text(stringResource(R.string.confirm),fontWeight = FontWeight.Bold)
                        }
                    }
                )

                if(chapters.isEmpty()){
                    EmptyIndicator(R.string.nothing_to_show_not_logged_in)
                }else{
                    LazyColumn {
                        items(chapters.size) { index ->
                            val (chapter, isSelected) = chapters[index]
                            ChapterItem(
                                chapter = chapter,
                                isSelected = isSelected,
                                onClick = { onToggle(index) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterItem(
    chapter: ChapterInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SizeTokens.Level80)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = chapter.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            }
        )
    }
}