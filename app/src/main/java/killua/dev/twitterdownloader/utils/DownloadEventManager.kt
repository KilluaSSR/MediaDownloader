package killua.dev.twitterdownloader.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadEventManager @Inject constructor() {
    private val _downloadCompletedFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val downloadCompletedFlow = _downloadCompletedFlow.asSharedFlow()

    fun notifyDownloadCompleted() {
        _downloadCompletedFlow.tryEmit(Unit)
    }
}