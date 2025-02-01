package killua.dev.base.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Singleton

@Singleton
class DownloadEventManager  {
    private val _downloadCompletedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val downloadCompletedFlow = _downloadCompletedFlow.asSharedFlow()

    fun notifyDownloadCompleted() {
        _downloadCompletedFlow.tryEmit(Unit)
    }
}