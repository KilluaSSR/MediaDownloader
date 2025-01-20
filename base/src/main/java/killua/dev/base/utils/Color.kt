package killua.dev.base.utils

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
fun Color.withState(enabled: Boolean = true) =
    if (enabled) this else this.copy(alpha = DisabledAlpha)

const val DisabledAlpha = 0.38f