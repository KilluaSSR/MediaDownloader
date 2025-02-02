package killua.dev.base.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import killua.dev.base.states.ThumbnailState
import killua.dev.base.ui.tokens.SizeTokens

@Composable
fun DownloadThumbnail(
    fileUri: Uri?,
    thumbnailCache: Map<Uri, Bitmap?>,
    modifier: Modifier = Modifier
) {
    val thumbnailState = when {
        fileUri != null && thumbnailCache[fileUri] != null ->
            ThumbnailState.Available(thumbnailCache[fileUri]!!)
        else -> ThumbnailState.Unavailable
    }

    when (thumbnailState) {
        is ThumbnailState.Available -> Image(
            bitmap = thumbnailState.bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = modifier
                .size(SizeTokens.Level72)
                .clip(RoundedCornerShape(SizeTokens.Level8)),
            contentScale = ContentScale.Crop
        )
        ThumbnailState.Unavailable -> Box(
            modifier = modifier
                .size(SizeTokens.Level72)
                .background(
                    Color.Gray.copy(alpha = 0.2f),
                    RoundedCornerShape(SizeTokens.Level8)
                )
        )
    }
}