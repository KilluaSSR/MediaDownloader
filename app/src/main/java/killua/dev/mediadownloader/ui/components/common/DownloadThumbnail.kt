package killua.dev.mediadownloader.ui.components.common

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import killua.dev.mediadownloader.Model.MediaType
import killua.dev.mediadownloader.R
import killua.dev.mediadownloader.states.ThumbnailState
import killua.dev.mediadownloader.ui.tokens.SizeTokens
import killua.dev.mediadownloader.utils.drawableToImageVector

@Composable
fun DownloadThumbnail(
    fileType: MediaType,
    fileUri: Uri?,
    thumbnailCache: Map<Uri, Bitmap?>,
    modifier: Modifier = Modifier
) {
    when(fileType){
        MediaType.PDF -> {
            Icon(
                imageVector = drawableToImageVector(R.drawable.baseline_picture_as_pdf_24),
                contentDescription = null,
                modifier = modifier
                    .size(SizeTokens.Level72)
                    .clip(RoundedCornerShape(SizeTokens.Level8)),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        else -> {
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

    }
}