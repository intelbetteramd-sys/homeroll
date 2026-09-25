package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanField
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

/**
 * The reversed path: the PC broadcasts "who is there" every second and the phone answers straight back.
 * Windows lets a unicast answer to a broadcast in for a few seconds without any firewall rule —
 * this is what the spike checks.
 */
class PhoneBroadcastFinder(private val pcName: String) {
    suspend fun run(targets: List<InetAddress>, startedAt: Long, onFound: (FoundPhone) -> Unit) =
        withContext(Dispatchers.IO) {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = LanDataConstants.RECEIVE_TIMEOUT_MILLIS
                val discover = lanMessage(LanSpikeConstants.DISCOVER, "pc" to pcName).toByteArray()
                val buffer = ByteArray(LanDataConstants.DATAGRAM_BUFFER_SIZE)
                val seen = mutableSetOf<String>()
                var nextSend = 0L
                while (isActive) {
                    if (System.currentTimeMillis() >= nextSend) {
                        targets.forEach {
                            socket.send(DatagramPacket(discover, discover.size, it, LanSpikeConstants.DISCOVERY_PORT))
                        }
                        nextSend = System.currentTimeMillis() + LanSpikeConstants.DISCOVER_INTERVAL_MILLIS
                    }
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket.receive(packet)
                    } catch (_: SocketTimeoutException) {
                        continue
                    }
                    val answer = String(packet.data, 0, packet.length)
                    val host = packet.address.hostAddress
                    if (answer.startsWith(LanSpikeConstants.PHONE_HERE) && seen.add(host)) {
                        onFound(
                            FoundPhone(
                                name = lanField(answer, "name") ?: host,
                                host = host,
                                port = lanField(answer, "port")?.toIntOrNull() ?: LanSpikeConstants.PHONE_PORT,
                                method = DiscoveryMethod.Broadcast,
                                foundAfterMillis = System.currentTimeMillis() - startedAt,
                            ),
                        )
                    }
                }
            }
        }
}
