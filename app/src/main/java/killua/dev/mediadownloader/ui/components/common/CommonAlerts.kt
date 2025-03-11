package killua.dev.mediadownloader.ui.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import killua.dev.mediadownloader.R
@Composable
fun DevelopingAlert(
    onDismiss: () -> Unit
) {
    OKAlert(stringResource(R.string.sorry),stringResource(R.string.under_dev),onDismiss)
}

@Composable
fun FileNotFountAlert(
    onDismiss: () -> Unit
) {
    OKAlert(stringResource(R.string.file_not_found),stringResource(R.string.file_not_found_desc),onDismiss)
}