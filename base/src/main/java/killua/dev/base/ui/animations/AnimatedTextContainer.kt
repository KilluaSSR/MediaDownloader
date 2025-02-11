package killua.dev.base.ui.animations


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import killua.dev.base.ui.tokens.AnimationTokens


@androidx.compose.animation.ExperimentalAnimationApi
@androidx.compose.runtime.Composable
fun AnimatedTextContainer(
    targetState: String,
    content: @androidx.compose.runtime.Composable AnimatedVisibilityScope.(targetState: String) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInVertically { height -> height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> -height } + fadeOut())
            } else {
                (slideInVertically { height -> -height } + fadeIn()).togetherWith(
                    slideOutVertically { height -> height } + fadeOut())
            }.using(
                SizeTransform(clip = false)
            )
        },
        label = AnimationTokens.AnimatedTextLabel,
        content = content
    )
}