package killua.dev.twitterdownloader.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import db.DownloadStatus
import killua.dev.base.ui.components.TopBar
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.twitterdownloader.Model.DownloadItem
enum class DownloadPageCommands{
    Open,
    Resume,
    Pause,
    Retry,
    Cancel,
    Delete
}

@Composable
fun DownloadPageTopAppBar(navController: NavHostController){
    TopBar(navController,"Downloaded"){
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadItem,
    onCommand: (DownloadItem, DownloadPageCommands) -> Unit
) {
    val status = item.downloadState.toDownloadStatus()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SizeTokens.Level6)
            .clickable {
                if (status == DownloadStatus.COMPLETED) {
                    onCommand(item, DownloadPageCommands.Open)
                }
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(SizeTokens.Level16)) {
            if (status == DownloadStatus.COMPLETED && item.fileUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(item.fileUri)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(SizeTokens.Level64)
                        .align(Alignment.CenterVertically),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(SizeTokens.Level64)
                        .alpha(0.3f)
                        .align(Alignment.CenterVertically),
                ) {

                }
            }

            Spacer(modifier = Modifier.width(SizeTokens.Level8))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = item.twitterScreenName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.twitterName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (status == DownloadStatus.COMPLETED) {
                    Text(
                        text = "Completed at: ${item.completedAt ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8)
            ) {
                when (status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Pause) }) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Cancel) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                    DownloadStatus.PENDING,
                    DownloadStatus.FAILED -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Resume) }) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        if (status == DownloadStatus.FAILED) {
                            IconButton(onClick = { onCommand(item, DownloadPageCommands.Retry) }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Delete) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}