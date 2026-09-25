package app.pixroost.desktop.spike.data

import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceListener

/**
 * The reversed path through mDNS: the PC browses for the phone's `_pixroost-phone._tcp`. Answers arrive as
 * multicast, so without a firewall rule Windows may drop them — the spike checks whether it does.
 */
class PhoneMdnsFinder(private val jmdns: JmDNS) {
    private var listener: ServiceListener? = null

    fun start(startedAt: Long, onFound: (FoundPhone) -> Unit) {
        val seen = mutableSetOf<String>()
        listener = object : ServiceListener {
            override fun serviceAdded(event: ServiceEvent) {
                jmdns.requestServiceInfo(event.type, event.name)
            }

            override fun serviceRemoved(event: ServiceEvent) = Unit

            override fun serviceResolved(event: ServiceEvent) {
                val host = event.info.inet4Addresses.firstOrNull()?.hostAddress ?: return
                if (!seen.add(host)) return
                onFound(
                    FoundPhone(
                        name = event.info.name,
                        host = host,
                        port = event.info.port,
                        method = DiscoveryMethod.Mdns,
                        foundAfterMillis = System.currentTimeMillis() - startedAt,
                    ),
                )
            }
        }.also { jmdns.addServiceListener(LanDataConstants.PHONE_SERVICE_TYPE_LOCAL, it) }
    }

    fun stop() {
        listener?.let { jmdns.removeServiceListener(LanDataConstants.PHONE_SERVICE_TYPE_LOCAL, it) }
    }
}
