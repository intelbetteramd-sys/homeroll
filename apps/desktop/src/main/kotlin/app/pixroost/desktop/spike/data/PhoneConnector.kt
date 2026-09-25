package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.lan.lanMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/** The reversed path: the PC opens the connection, so Windows sees only outgoing traffic. */
suspend fun connectToPhone(host: String, port: Int, pcName: String): ConnectResult = withContext(Dispatchers.IO) {
    val start = System.currentTimeMillis()
    try {
        Socket().use { socket ->
            socket.connect(InetSocketAddress(host, port), LanSpikeConstants.CONNECT_TIMEOUT_MILLIS)
            val connectMillis = System.currentTimeMillis() - start
            socket.soTimeout = LanSpikeConstants.READ_TIMEOUT_MILLIS
            socket.getOutputStream().write((lanMessage(LanSpikeConstants.HELLO, "pc" to pcName) + "\n").toByteArray())
            val answer = socket.getInputStream().bufferedReader().readLine()
            ConnectResult(connectMillis, System.currentTimeMillis() - start, answer, null)
        }
    } catch (error: IOException) {
        ConnectResult(null, null, null, "${error.javaClass.simpleName}: ${error.message}")
    }
}
