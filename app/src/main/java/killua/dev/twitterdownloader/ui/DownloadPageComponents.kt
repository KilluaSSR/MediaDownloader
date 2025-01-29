package killua.dev.twitterdownloader.ui

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import db.DownloadStatus
import killua.dev.base.ui.components.TopBar
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.twitterdownloader.Model.DownloadItem
import killua.dev.twitterdownloader.utils.formatTimestamp

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
    val context = LocalContext.current
    val status = item.downloadState.toDownloadStatus()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = status == DownloadStatus.COMPLETED) {
                item.fileUri?.let { uri ->
                    val openIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "video/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(openIntent)
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧封面图
            if (status == DownloadStatus.COMPLETED && item.fileUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(item.fileUri),
                    contentDescription = "Downloaded Video Thumbnail",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 中间内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.twitterName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "@${item.twitterScreenName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // 状态显示
                when (status) {
                    DownloadStatus.DOWNLOADING -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { item.progress / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (item.progress >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "${item.progress}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (item.progress >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                    DownloadStatus.COMPLETED -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "完成时间: ${formatTimestamp(item.completedAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    DownloadStatus.FAILED -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "下载失败",
                            style = MaterialTheme.typography.bodySmall,color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 右侧操作按钮
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (status) {
                    DownloadStatus.DOWNLOADING -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Pause) }) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.Gray)
                        }
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Cancel) }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray)
                        }
                    }
                    DownloadStatus.PENDING, DownloadStatus.FAILED -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Resume) }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = MaterialTheme.colorScheme.primary)
                        }
                        if (status == DownloadStatus.FAILED) {
                            IconButton(onClick = { onCommand(item, DownloadPageCommands.Retry) }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                    DownloadStatus.COMPLETED -> {
                        IconButton(onClick = { onCommand(item, DownloadPageCommands.Delete) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}