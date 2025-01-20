package killua.dev.twitterdownloader.Model

import killua.dev.twitterdownloader.ui.UIState

data class DownloadUIState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : UIState