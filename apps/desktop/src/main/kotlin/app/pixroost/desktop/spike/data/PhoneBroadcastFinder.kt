package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanField
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

/**
 * The reversed path: the PC broadcasts "who is there" every second and the phone answers straight back.
 * Windows lets a unicast answer to a broadcast in for a few seconds without any firewall rule —
 * this is what the spike checks. A network error (an adapter went down, a VPN came up) is reported once
 * and does not stop the search.
 */
class PhoneBroadcastFinder(private val pcName: String, private val onError: (String) -> Unit) {
    private val discover = lanMessage(LanSpikeConstants.DISCOVER, "pc" to pcName).toByteArray()
    private val buffer = ByteArray(LanDataConstants.DATAGRAM_BUFFER_SIZE)
    private val reportedErrors = mutableSetOf<String>()

    suspend fun run(targets: List<InetAddress>, startedAt: Long, onFound: (FoundPhone) -> Unit) =
        withContext(Dispatchers.IO) {
            DatagramSocket().use { socket ->
                socket.broadcast = true
                socket.soTimeout = LanDataConstants.RECEIVE_TIMEOUT_MILLIS
                val seen = mutableSetOf<String>()
                var nextSend = 0L
                while (isActive) {
                    if (System.currentTimeMillis() >= nextSend) {
                        targets.forEach { send(socket, it) }
                        nextSend = System.currentTimeMillis() + LanSpikeConstants.DISCOVER_INTERVAL_MILLIS
                    }
                    receive(socket, startedAt)?.takeIf { seen.add(it.host) }?.let(onFound)
                }
            }
        }

    private fun send(socket: DatagramSocket, target: InetAddress) {
        try {
            socket.send(DatagramPacket(discover, discover.size, target, LanSpikeConstants.DISCOVERY_PORT))
        } catch (error: IOException) {
            report("Рассылка на ${target.hostAddress} не ушла: ${error.message}")
        }
    }

    /** Waits for one datagram; returns the phone if it is an answer to the broadcast. */
    private suspend fun receive(socket: DatagramSocket, startedAt: Long): FoundPhone? {
        val packet = DatagramPacket(buffer, buffer.size)
        val received = try {
            socket.receive(packet)
            true
        } catch (_: SocketTimeoutException) {
            false
        } catch (error: IOException) {
            report("Ответ на рассылку не принят: ${error.message}")
            delay(LanDataConstants.RECEIVE_TIMEOUT_MILLIS.toLong())
            false
        }
        val answer = if (received) String(packet.data, 0, packet.length) else ""
        val host = packet.address?.hostAddress.orEmpty()
        return if (answer.startsWith(LanSpikeConstants.PHONE_HERE)) {
            FoundPhone(
                name = lanField(answer, "name") ?: host,
                host = host,
                port = lanField(answer, "port")?.toIntOrNull() ?: LanSpikeConstants.PHONE_PORT,
                method = DiscoveryMethod.Broadcast,
                foundAfterMillis = System.currentTimeMillis() - startedAt,
            )
        } else {
            null
        }
    }

    private fun report(text: String) {
        if (reportedErrors.add(text)) onError(text)
    }
}
