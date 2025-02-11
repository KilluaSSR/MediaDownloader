package killua.dev.twitterdownloader.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import db.DownloadStatus
import killua.dev.base.Model.DownloadPageCommands
import killua.dev.base.Model.DownloadProgress
import killua.dev.base.ui.components.DownloadActions
import killua.dev.base.ui.components.DownloadThumbnail
import killua.dev.base.ui.components.ShowStatus
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.mediaClickable
import killua.dev.twitterdownloader.Model.DownloadedItem

@Composable
fun DownloadItemCard(
    item: DownloadedItem,
    thumbnailCache: Map<Uri, Bitmap?>,
    onCommand: (DownloadedItem, DownloadPageCommands) -> Unit,
    downloadProgress: Map<String, DownloadProgress>,
    modifier: Modifier = Modifier,
    fileNotFoundClick: () -> Unit
) {
    val context = LocalContext.current
    val status = item.downloadState.toDownloadStatus()
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SizeTokens.Level16, vertical = SizeTokens.Level1)
            .mediaClickable(
                context = context,
                status = status,
                fileUri = item.fileUri,
                fileNotFoundClick = fileNotFoundClick
            ),
        shape = RoundedCornerShape(SizeTokens.Level12),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = SizeTokens.Level4)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DownloadThumbnail(
                fileUri = if (status == DownloadStatus.COMPLETED) item.fileUri else null,
                thumbnailCache = thumbnailCache
            )

            Spacer(modifier = Modifier.width(SizeTokens.Level12))

            // 中间内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${item.screenName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                status.ShowStatus(
                    downloadProgress = downloadProgress,
                    downloadId = item.id,
                    createdAt = item.createdAt
                )
            }

            Spacer(modifier = Modifier.width(SizeTokens.Level8))

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level2)
            ) {
                DownloadActions(
                    status = status,
                    onCommand = { command -> onCommand(item, command) }
                )
            }
        }
    }
}

@Composable
fun Loading(){
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Loading...",
                modifier = Modifier.alpha(0.3f)
            )

            Spacer(modifier = Modifier.size(SizeTokens.Level16))
            LinearProgressIndicator(
                modifier = Modifier
                    .size(width = SizeTokens.Level128, height = SizeTokens.Level8)
            )
        }
    }
}