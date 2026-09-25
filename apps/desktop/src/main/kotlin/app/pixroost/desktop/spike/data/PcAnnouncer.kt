package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import java.io.Closeable
import java.net.InetAddress
import javax.jmdns.JmDNS
import javax.jmdns.ServiceInfo

/** Announces this PC as `_pixroost._tcp` with JmDNS, so the phone can find it (the direct path). */
class PcAnnouncer(address: InetAddress, pcName: String) : Closeable {
    val jmdns: JmDNS = JmDNS.create(address, pcName)

    init {
        val info = ServiceInfo.create(
            "${LanSpikeConstants.PC_SERVICE_TYPE}.local.",
            "Pixroost $pcName",
            LanSpikeConstants.PC_PORT,
            0,
            0,
            mapOf("id" to pcName, "v" to "1"),
        )
        jmdns.registerService(info)
    }

    override fun close() {
        jmdns.unregisterAllServices()
        jmdns.close()
    }
}
