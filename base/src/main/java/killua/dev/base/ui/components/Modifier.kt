package killua.dev.base.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp

fun androidx.compose.ui.Modifier.paddingStart(start: androidx.compose.ui.unit.Dp) =
    padding(start, 0.dp, 0.dp, 0.dp)

fun androidx.compose.ui.Modifier.paddingTop(top: androidx.compose.ui.unit.Dp) =
    padding(0.dp, top, 0.dp, 0.dp)

fun androidx.compose.ui.Modifier.paddingEnd(end: androidx.compose.ui.unit.Dp) =
    padding(0.dp, 0.dp, end, 0.dp)

fun androidx.compose.ui.Modifier.paddingBottom(bottom: androidx.compose.ui.unit.Dp) =
    padding(0.dp, 0.dp, 0.dp, bottom)

fun androidx.compose.ui.Modifier.paddingHorizontal(horizontal: androidx.compose.ui.unit.Dp) =
    padding(horizontal, 0.dp)

fun androidx.compose.ui.Modifier.paddingVertical(vertical: androidx.compose.ui.unit.Dp) =
    padding(0.dp, vertical)