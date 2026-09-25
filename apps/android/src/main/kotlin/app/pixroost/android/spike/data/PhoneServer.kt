package app.pixroost.android.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException

/** The reversed path: the phone waits for the PC to connect. Android has no inbound firewall. */
class PhoneServer(private val phoneName: String) {
    private var socket: ServerSocket? = null

    /** Runs until cancelled; reports every greeting it answered. */
    suspend fun run(onGreeting: (from: String, message: String) -> Unit) = withContext(Dispatchers.IO) {
        val server = ServerSocket(LanSpikeConstants.PHONE_PORT).also { socket = it }
        try {
            while (isActive) {
                server.accept().use { client ->
                    try {
                        client.soTimeout = LanSpikeConstants.READ_TIMEOUT_MILLIS
                        val message = client.getInputStream().bufferedReader().readLine().orEmpty()
                        val welcome = lanMessage(LanSpikeConstants.WELCOME, "phone" to phoneName) + "\n"
                        client.getOutputStream().write(welcome.toByteArray())
                        onGreeting(client.inetAddress.hostAddress.orEmpty(), message)
                    } catch (error: IOException) {
                        onGreeting(client.inetAddress.hostAddress.orEmpty(), "error: ${error.message}")
                    }
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
