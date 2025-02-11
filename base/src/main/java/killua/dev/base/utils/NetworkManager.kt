package killua.dev.base.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class NetworkState {
    object Available : NetworkState()
    object WifiConnected : NetworkState()
    data class Unavailable(val reason: String? = null) : NetworkState()
}

@Singleton
class NetworkManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Unavailable())

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)

            _networkState.value = when {
                !hasInternet -> NetworkState.Unavailable("无网络连接")
                hasWifi -> NetworkState.WifiConnected
                else -> NetworkState.Available
            }
        }

        override fun onLost(network: Network) {
            _networkState.value = NetworkState.Unavailable("网络连接已断开")
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    fun isWifiConnected(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    }

    fun isNetworkAvailable(): Boolean {
        val capabilities = connectivityManager.getNetworkCapabilities(
            connectivityManager.activeNetwork
        )
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}