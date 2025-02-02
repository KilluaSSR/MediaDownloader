package killua.dev.base.Model

data class DownloadProgress(
    val progress: Int = 0,
    val isCompleted: Boolean = false,
    val isFailed: Boolean = false,
    val errorMessage: String? = null
)