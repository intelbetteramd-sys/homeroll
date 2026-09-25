package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.transfer.TransferSpikeConstants
import app.pixroost.desktop.spike.util.pipe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * The reversed path on the PC. For every phone found by the broadcast the PC keeps a few idle connections
 * open to it. When the phone starts talking on one, the PC connects to its own HTTPS server on 127.0.0.1
 * and pipes the bytes both ways, so TLS runs end to end between the phone and the server. Idle connections
 * are replaced every few seconds, so one that died with the Wi-Fi is never handed to the phone.
 */
class ReverseLinks(private val scope: CoroutineScope, private val onEvent: (String) -> Unit) {
    private val _stats = MutableStateFlow<Map<String, ReverseLinkStats>>(emptyMap())
    val stats: StateFlow<Map<String, ReverseLinkStats>> = _stats.asStateFlow()
    private val jobs = mutableMapOf<String, Job>()

    fun open(host: String, port: Int) {
        if (jobs[host]?.isActive == true) return
        onEvent("Развёрнутый путь: держу ${TransferSpikeConstants.REVERSE_POOL_SIZE} соединения к $host:$port")
        jobs[host] = scope.launch(Dispatchers.IO) { keepPool(host, port) }
    }

    fun closeAll() {
        jobs.values.forEach { it.cancel() }
        jobs.clear()
        _stats.value = emptyMap()
    }

    private suspend fun keepPool(host: String, port: Int) {
        var lastError: String? = null
        while (currentCoroutineContext().isActive) {
            if ((_stats.value[host]?.idle ?: 0) >= TransferSpikeConstants.REVERSE_POOL_SIZE) {
                delay(LanDataConstants.REVERSE_POLL_MILLIS)
                continue
            }
            try {
                val socket = Socket().apply {
                    connect(InetSocketAddress(host, port), LanSpikeConstants.CONNECT_TIMEOUT_MILLIS)
                    tcpNoDelay = true
                }
                change(host) { it.copy(idle = it.idle + 1, opened = it.opened + 1) }
                lastError = null
                scope.launch(Dispatchers.IO) { serve(host, socket) }
            } catch (error: IOException) {
                change(host) { it.copy(failed = it.failed + 1) }
                if (error.message != lastError) onEvent("Не открыл соединение к телефону $host: ${error.message}")
                lastError = error.message
                delay(LanDataConstants.REVERSE_RETRY_MILLIS)
            }
        }
    }

    /** Waits for the phone's first bytes on an idle connection, then relays it to the local server. */
    private suspend fun serve(host: String, phone: Socket) {
        val buffer = ByteArray(TransferSpikeConstants.COPY_BUFFER_SIZE)
        phone.soTimeout = TransferSpikeConstants.REVERSE_IDLE_MILLIS
        val read = try {
            phone.getInputStream().read(buffer)
        } catch (_: SocketTimeoutException) {
            -1
        } catch (_: IOException) {
            -1
        }
        change(host) { it.copy(idle = it.idle - 1) }
        if (read < 0) {
            phone.close()
            return
        }
        change(host) { it.copy(active = it.active + 1) }
        try {
            phone.soTimeout = 0
            val server = Socket(LanDataConstants.LOOPBACK, LanSpikeConstants.PC_PORT).apply { tcpNoDelay = true }
            pipe(phone, server, prefix = buffer.copyOf(read))
        } catch (error: IOException) {
            onEvent("Развёрнутое соединение с $host оборвалось: ${error.message}")
            phone.close()
        } finally {
            change(host) { it.copy(active = it.active - 1) }
        }
    }

    private fun change(host: String, transform: (ReverseLinkStats) -> ReverseLinkStats) {
        _stats.update { it + (host to transform(it[host] ?: ReverseLinkStats())) }
    }
}
