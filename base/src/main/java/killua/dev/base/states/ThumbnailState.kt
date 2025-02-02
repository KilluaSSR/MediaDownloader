package killua.dev.base.states

import android.graphics.Bitmap

sealed class ThumbnailState {
    data class Available(val bitmap: Bitmap) : ThumbnailState()
    object Unavailable : ThumbnailState()
}