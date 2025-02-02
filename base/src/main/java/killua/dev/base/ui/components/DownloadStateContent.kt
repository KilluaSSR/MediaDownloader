package killua.dev.base.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import db.DownloadStateUI
import db.DownloadStatus
import killua.dev.base.Model.DownloadProgress
import killua.dev.base.ui.tokens.SizeTokens
import killua.dev.base.utils.formatTimestamp

@Composable
fun DownloadStateContent(stateUI: DownloadStateUI) {
    when {
        stateUI.progressInfo != null -> {
            Spacer(modifier = Modifier.height(SizeTokens.Level8))
            LinearProgressIndicator(
                progress = { stateUI.progressInfo.progress / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = if (stateUI.progressInfo.progress >= 50)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "${stateUI.progressInfo.progress}%",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = if (stateUI.progressInfo.progress >= 50)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        }
        stateUI.completionInfo != null -> {
            Spacer(modifier = Modifier.height(SizeTokens.Level4))
            Text(
                text = "Completed at: ${formatTimestamp(stateUI.completionInfo.completedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        stateUI.failureInfo != null -> {
            Spacer(modifier = Modifier.height(SizeTokens.Level4))
            Text(
                text = stateUI.failureInfo.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun ShowDownloading(
    progress: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(SizeTokens.Level8))
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = if (progress >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
        Text(
            text = "${progress}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = if (progress >= 50) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ShowCompleted(createdAt: Long) {
    Column {
        Spacer(modifier = Modifier.height(SizeTokens.Level4))
        Text(
            text = "Completed at: ${formatTimestamp(createdAt)}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
private fun ShowFailed() {
    Column {
        Spacer(modifier = Modifier.height(SizeTokens.Level4))
        Text(
            text = "Failed",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun DownloadStatus.ShowStatus(
    downloadProgress: Map<String, DownloadProgress>,
    downloadId: String,
    createdAt: Long = 0L
) {
    when (this) {
        DownloadStatus.DOWNLOADING -> ShowDownloading(
            progress = downloadProgress[downloadId]?.progress ?: 0
        )
        DownloadStatus.COMPLETED -> ShowCompleted(createdAt)
        DownloadStatus.FAILED -> ShowFailed()
        else -> {}
    }
}