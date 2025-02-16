package killua.dev.mediadownloader.ui.animations


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import killua.dev.mediadownloader.ui.tokens.AnimationTokens


@ExperimentalAnimationApi
@Composable
fun AnimatedTextContainer(
    targetState: String,
    content: @Composable AnimatedVisibilityScope.(targetState: String) -> Unit,
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