package app.pixroost.android.spike.data

import app.pixroost.android.spike.util.pipe
import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.transfer.TransferSpikeConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.LinkedBlockingDeque

/**
 * The reversed path on the phone. The PC opens connections to port 47201 and they wait here. The upload client
 * connects to [localPort] on 127.0.0.1; each such connection is joined with a waiting one from the PC, so the
 * client speaks HTTPS to the PC's server without connecting to the PC itself.
 */
class ReverseRelay(private val onEvent: (String) -> Unit) {
    private val waiting = LinkedBlockingDeque<WaitingConnection>()
    private var fromPc: ServerSocket? = null
    private var local: ServerSocket? = null
    private val _waitingCount = MutableStateFlow(0)
    val waitingCount: StateFlow<Int> = _waitingCount.asStateFlow()

    val localPort: Int get() = local?.localPort ?: 0

    suspend fun run() = coroutineScope {
        try {
            val pcServer = ServerSocket().apply {
                reuseAddress = true
                bind(InetSocketAddress(LanSpikeConstants.PHONE_PORT))
            }.also { fromPc = it }
            val loopback = InetAddress.getByName(LanDataConstants.LOOPBACK)
            val localServer = ServerSocket(0, LanDataConstants.LOCAL_BACKLOG, loopback).also { local = it }
            launch(Dispatchers.IO) { acceptFromPc(pcServer) }
            launch(Dispatchers.IO) { acceptLocal(localServer, this@coroutineScope) }
        } catch (error: IOException) {
            onEvent("Развёрнутый путь не запустился: ${error.message}")
        }
    }

    fun close() {
        fromPc?.close()
        local?.close()
        generateSequence { waiting.poll() }.forEach { it.socket.close() }
    }

    private fun acceptFromPc(server: ServerSocket) {
        try {
            while (true) {
                val socket = server.accept().apply { tcpNoDelay = true }
                pruneOld()
                waiting.add(WaitingConnection(socket, System.currentTimeMillis()))
                _waitingCount.value = waiting.size
            }
        } catch (_: IOException) {
            // Closed when the screen goes away.
        }
    }

    /** The PC replaces idle connections every few seconds; the old ones are closed here, so they do not pile up. */
    private fun pruneOld() {
        val now = System.currentTimeMillis()
        waiting.filter { now - it.acceptedAt > TransferSpikeConstants.REVERSE_MAX_AGE_MILLIS }.forEach {
            if (waiting.remove(it)) it.socket.close()
        }
    }

    private fun acceptLocal(server: ServerSocket, scope: CoroutineScope) {
        try {
            while (true) {
                val client = server.accept().apply { tcpNoDelay = true }
                scope.launch(Dispatchers.IO) { relay(client) }
            }
        } catch (_: IOException) {
            // Closed when the screen goes away.
        }
    }

    private suspend fun relay(client: Socket) {
        val pc = takeFresh()
        if (pc == null) {
            onEvent("Развёрнутый путь: ПК не открыл соединение к телефону")
            client.close()
            return
        }
        pipe(client, pc)
    }

    /** A connection from the PC that is young enough to be alive, waiting a bit for one if there is none. */
    private suspend fun takeFresh(): Socket? {
        val deadline = System.currentTimeMillis() + LanDataConstants.WAIT_FOR_PC_MILLIS
        while (System.currentTimeMillis() < deadline) {
            val next = waiting.poll()
            _waitingCount.value = waiting.size
            when {
                next == null -> delay(LanDataConstants.WAIT_POLL_MILLIS)

                System.currentTimeMillis() - next.acceptedAt > TransferSpikeConstants.REVERSE_MAX_AGE_MILLIS ->
                    next.socket.close()

                else -> return next.socket
            }
        }
        return null
    }
}
