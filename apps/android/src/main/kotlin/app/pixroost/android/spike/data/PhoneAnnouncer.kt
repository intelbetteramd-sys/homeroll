package app.pixroost.android.spike.data

import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import app.pixroost.core.spike.lan.LanSpikeConstants

/** The reversed path through mDNS: announces the phone as `_pixroost-phone._tcp` for the PC to find. */
class PhoneAnnouncer(private val nsd: NsdManager) {
    private var listener: NsdManager.RegistrationListener? = null

    fun start(phoneName: String, onLog: (String) -> Unit) {
        val info = NsdServiceInfo().apply {
            serviceName = "Pixroost $phoneName"
            serviceType = LanSpikeConstants.PHONE_SERVICE_TYPE
            port = LanSpikeConstants.PHONE_PORT
        }
        listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) = onLog("Телефон объявлен как «${info.serviceName}»")

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) =
                onLog("Объявить телефон не удалось, код $errorCode")

            override fun onServiceUnregistered(info: NsdServiceInfo) = Unit

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) = Unit
        }.also { nsd.registerService(info, NsdManager.PROTOCOL_DNS_SD, it) }
    }

    fun stop() {
        listener?.let { runCatching { nsd.unregisterService(it) } }
        listener = null
    }
}
