package killua.dev.mediadownloader.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import killua.dev.mediadownloader.Model.themeSettingItems
import killua.dev.mediadownloader.ui.components.common.BottomSheet
import killua.dev.mediadownloader.ui.components.common.BottomSheetItem
import killua.dev.mediadownloader.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState,
    onThemeSelected: suspend (ThemeMode) -> Unit
) {
    val scope = rememberCoroutineScope()

    BottomSheet(onDismiss, sheetState) {
        themeSettingItems.forEach { item ->
            BottomSheetItem(
                icon = item.icon,
                text = stringResource(item.titleRes)
            ) {
                scope.launch {
                    onThemeSelected(item.mode)
                    onDismiss()
                }
            }
        }
    }
}