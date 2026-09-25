package app.pixroost.android.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

/**
 * The reversed path: answers the PC's "who is there" broadcast straight back to the PC, so the PC learns the
 * phone's address without receiving anything it did not ask for. A failed answer is reported and the
 * responder keeps listening.
 */
class DiscoveryResponder(private val phoneName: String) {
    private var socket: DatagramSocket? = null
    private val reply = lanMessage(
        LanSpikeConstants.PHONE_HERE,
        "port" to LanSpikeConstants.PHONE_PORT.toString(),
        "name" to phoneName,
    ).toByteArray()

    suspend fun run(onRequest: (from: String) -> Unit, onError: (String) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val udp = DatagramSocket(null).apply {
                reuseAddress = true
                broadcast = true
                bind(InetSocketAddress(LanSpikeConstants.DISCOVERY_PORT))
            }.also { socket = it }
            val buffer = ByteArray(LanDataConstants.DATAGRAM_BUFFER_SIZE)
            while (isActive) {
                val packet = DatagramPacket(buffer, buffer.size)
                udp.receive(packet)
                if (String(packet.data, 0, packet.length).startsWith(LanSpikeConstants.DISCOVER)) {
                    answer(udp, packet, onRequest, onError)
                }
            }
        } catch (error: IOException) {
            // Closing the socket when the screen goes away lands here too; the log is gone by then.
            onError("Ответчик на рассылку остановлен: ${error.message}")
        }
    }

    fun close() {
        socket?.close()
    }

    private fun answer(
        udp: DatagramSocket,
        request: DatagramPacket,
        onRequest: (from: String) -> Unit,
        onError: (String) -> Unit,
    ) {
        val from = request.address.hostAddress.orEmpty()
        try {
            udp.send(DatagramPacket(reply, reply.size, request.address, request.port))
            onRequest(from)
        } catch (error: IOException) {
            onError("Ответ ПК $from не ушёл: ${error.message}")
        }
    }
}
