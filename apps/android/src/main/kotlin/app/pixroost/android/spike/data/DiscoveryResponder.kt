package app.pixroost.android.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketException

/**
 * The reversed path: answers the PC's "who is there" broadcast straight back to the PC, so the PC learns the
 * phone's address without receiving anything it did not ask for.
 */
class DiscoveryResponder(private val phoneName: String) {
    private var socket: DatagramSocket? = null

    suspend fun run(onRequest: (from: String) -> Unit) = withContext(Dispatchers.IO) {
        val udp = DatagramSocket(null).apply {
            reuseAddress = true
            broadcast = true
            bind(InetSocketAddress(LanSpikeConstants.DISCOVERY_PORT))
        }.also { socket = it }
        val reply = lanMessage(
            LanSpikeConstants.PHONE_HERE,
            "port" to LanSpikeConstants.PHONE_PORT.toString(),
            "name" to phoneName,
        ).toByteArray()
        val buffer = ByteArray(LanDataConstants.DATAGRAM_BUFFER_SIZE)
        try {
            while (isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                udp.receive(packet)
                if (String(packet.data, 0, packet.length).startsWith(LanSpikeConstants.DISCOVER)) {
                    udp.send(DatagramPacket(reply, reply.size, packet.address, packet.port))
                    onRequest(packet.address.hostAddress.orEmpty())
                }
            }
        } catch (_: SocketException) {
            // The socket was closed when the screen went away.
        }
    }

    fun close() {
        socket?.close()
    }
}
