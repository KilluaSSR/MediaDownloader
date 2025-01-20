package killua.dev.twitterdownloader.Model

data class DownloadUIState(
    val downloads: List<DownloadItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) : killua.dev.base.ui.UIState