package killua.dev.mediadownloader.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
fun drawableToImageVector(@DrawableRes id: Int): ImageVector {
    return ImageVector.vectorResource(id = id)
}