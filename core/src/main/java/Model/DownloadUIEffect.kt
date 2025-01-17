package Model

import db.DownloadState
import ui.UIEffect

sealed interface DownloadUIEffect : UIEffect {
    data class ShowError(val message: String) : DownloadUIEffect
    data class DownloadStateChanged(
        val id: String,
        val state: DownloadState
    ) : DownloadUIEffect
}