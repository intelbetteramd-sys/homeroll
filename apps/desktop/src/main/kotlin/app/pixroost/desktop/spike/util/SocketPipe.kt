package app.pixroost.desktop.spike.util

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.Socket

/**
 * Copies bytes both ways, then closes both sockets. [prefix] is what was already read from [first] and goes to
 * [second] before the rest. When one direction ends, the other gets a few seconds to end too and is then cut:
 * a peer that vanished with the Wi-Fi never closes its side.
 */
suspend fun pipe(first: Socket, second: Socket, prefix: ByteArray = ByteArray(0)) {
    try {
        coroutineScope {
            val oneWayDone = CompletableDeferred<Unit>()
            val copies = listOf(
                launch(Dispatchers.IO) {
                    copyThenShutdown(first, second, prefix)
                    oneWayDone.complete(Unit)
                },
                launch(Dispatchers.IO) {
                    copyThenShutdown(second, first, ByteArray(0))
                    oneWayDone.complete(Unit)
                },
            )
            oneWayDone.await()
            withTimeoutOrNull(TransferSpikeConstants.PIPE_CLOSE_GRACE_MILLIS) { copies.joinAll() }
            first.close()
            second.close()
        }
    } finally {
        first.close()
        second.close()
    }
}

/** A failure closes both sockets, so the copy in the other direction stops too. */
private fun copyThenShutdown(from: Socket, to: Socket, prefix: ByteArray) {
    try {
        to.getOutputStream().write(prefix)
        from.getInputStream().copyTo(to.getOutputStream(), TransferSpikeConstants.COPY_BUFFER_SIZE)
        to.shutdownOutput()
    } catch (_: IOException) {
        from.close()
        to.close()
    }
}
