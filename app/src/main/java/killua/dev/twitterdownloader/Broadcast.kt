package killua.dev.twitterdownloader

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
const val DOWNLOAD_COMPLETED_ACTION = "killua.dev.twitterdownloader.DOWNLOAD_COMPLETED"
@Singleton
class DownloadEventManager @Inject constructor(
    @ApplicationContext private val context: Context
) : LifecycleEventObserver {
    private val _downloadCompletedFlow = MutableSharedFlow<Unit>()
    val downloadCompletedFlow = _downloadCompletedFlow.asSharedFlow()
    private val downloadCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DOWNLOAD_COMPLETED_ACTION) {
                CoroutineScope(Dispatchers.Main).launch {
                    _downloadCompletedFlow.emit(Unit)
                }
            }
        }
    }
    init {
        ContextCompat.registerReceiver(
            context,
            downloadCompletedReceiver,
            IntentFilter(DOWNLOAD_COMPLETED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            context.unregisterReceiver(downloadCompletedReceiver)
        }
    }
}