package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ServerSocket
import java.net.SocketException

/**
 * Accepts connections from the phone (the direct path). Needs an inbound firewall rule on Windows:
 * binding the port is the moment Windows shows its "Allow access" prompt.
 */
class PcServer(private val pcName: String) {
    private var socket: ServerSocket? = null

    /** Runs until cancelled; reports every greeting it answered. */
    suspend fun run(onGreeting: (from: String, message: String) -> Unit) = withContext(Dispatchers.IO) {
        val server = ServerSocket(LanSpikeConstants.PC_PORT).also { socket = it }
        try {
            while (isActive) {
                server.accept().use { client ->
                    try {
                        client.soTimeout = LanSpikeConstants.READ_TIMEOUT_MILLIS
                        val message = client.getInputStream().bufferedReader().readLine().orEmpty()
                        val welcome = lanMessage(LanSpikeConstants.WELCOME, "pc" to pcName) + "\n"
                        client.getOutputStream().write(welcome.toByteArray())
                        onGreeting(client.inetAddress.hostAddress, message)
                    } catch (error: IOException) {
                        onGreeting(client.inetAddress.hostAddress, "error: ${error.message}")
                    }
                }
            }
        } catch (_: SocketException) {
            // The socket was closed on exit.
        }
    }

    fun close() {
        socket?.close()
    }
}
