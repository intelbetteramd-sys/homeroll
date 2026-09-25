package app.pixroost.android.spike.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

/**
 * Reports when Wi-Fi comes and goes, so the log shows how long a transfer takes to resume after a break.
 * Callbacks come on a system thread.
 */
class WifiWatcher(context: Context, private val onChange: (String) -> Unit) {
    private val connectivity = context.getSystemService(ConnectivityManager::class.java)
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = onChange("Wi-Fi подключён")

        override fun onLost(network: Network) = onChange("Wi-Fi пропал")
    }

    fun start() {
        val request = NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
        connectivity.registerNetworkCallback(request, callback)
    }

    fun stop() {
        connectivity.unregisterNetworkCallback(callback)
    }
}
