package app.pixroost.android.spike.data

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import app.pixroost.core.spike.lan.LanSpikeConstants
import java.net.Inet4Address
import java.util.concurrent.Executors

/** The direct path: finds the PC's `_pixroost._tcp` service and resolves its address. */
class PcDiscovery(private val nsd: NsdManager) {
    private var listener: NsdManager.DiscoveryListener? = null
    private val executor = Executors.newSingleThreadExecutor()

    fun start(onFound: (FoundPc) -> Unit, onLog: (String) -> Unit) {
        val startedAt = SystemClock.elapsedRealtime()
        listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = onLog("Поиск ПК начат")

            override fun onServiceFound(info: NsdServiceInfo) {
                val foundAfter = SystemClock.elapsedRealtime() - startedAt
                onLog("Найден «${info.serviceName}» за $foundAfter мс, узнаю адрес")
                resolve(info, onLog) { host, port ->
                    val resolvedAfter = SystemClock.elapsedRealtime() - startedAt
                    onFound(FoundPc(info.serviceName, host, port, foundAfter, resolvedAfter))
                }
            }

            override fun onServiceLost(info: NsdServiceInfo) = onLog("ПК «${info.serviceName}» пропал из сети")

            override fun onDiscoveryStopped(serviceType: String) = Unit

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) =
                onLog("Поиск не запустился, код $errorCode")

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }.also { nsd.discoverServices(LanSpikeConstants.PC_SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, it) }
    }

    fun stop() {
        listener?.let { runCatching { nsd.stopServiceDiscovery(it) } }
        listener = null
    }

    private fun resolve(info: NsdServiceInfo, onLog: (String) -> Unit, onResolved: (host: String, port: Int) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            resolveWithCallback(info, onLog, onResolved)
        } else {
            @Suppress("DEPRECATION")
            nsd.resolveService(
                info,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) =
                        onLog("Адрес «${info.serviceName}» не получен, код $errorCode")

                    override fun onServiceResolved(info: NsdServiceInfo) {
                        @Suppress("DEPRECATION")
                        info.host?.hostAddress?.let { onResolved(it, info.port) }
                    }
                },
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun resolveWithCallback(
        info: NsdServiceInfo,
        onLog: (String) -> Unit,
        onResolved: (host: String, port: Int) -> Unit,
    ) {
        nsd.registerServiceInfoCallback(
            info,
            executor,
            object : NsdManager.ServiceInfoCallback {
                override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) =
                    onLog("Адрес «${info.serviceName}» не получен, код $errorCode")

                override fun onServiceUpdated(serviceInfo: NsdServiceInfo) {
                    val host = serviceInfo.hostAddresses.firstOrNull { it is Inet4Address } ?: return
                    nsd.unregisterServiceInfoCallback(this)
                    onResolved(host.hostAddress.orEmpty(), serviceInfo.port)
                }

                override fun onServiceLost() = Unit

                override fun onServiceInfoCallbackUnregistered() = Unit
            },
        )
    }
}
