package killua.dev.mediadownloader.Model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.ui.graphics.vector.ImageVector
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.ui.theme.ThemeMode

data class ThemeSettingItem(
    val mode: ThemeMode,
    @StringRes val titleRes: Int,
    val icon: ImageVector
)

val themeSettingItems = listOf(
    ThemeSettingItem(
        mode = ThemeMode.SYSTEM,
        titleRes = R.string.system,
        icon = Icons.Rounded.BrightnessAuto
    ),
    ThemeSettingItem(
        mode = ThemeMode.LIGHT,
        titleRes = R.string.light,
        icon = Icons.Rounded.LightMode
    ),
    ThemeSettingItem(
        mode = ThemeMode.DARK,
        titleRes = R.string.dark,
        icon = Icons.Rounded.DarkMode
    )
)