package app.pixroost.desktop.spike.util

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.Socket

/**
 * Copies bytes both ways until both sides finish or one fails, then closes both sockets. [prefix] is what was
 * already read from [first] and goes to [second] before the rest.
 */
suspend fun pipe(first: Socket, second: Socket, prefix: ByteArray = ByteArray(0)) {
    try {
        coroutineScope {
            launch(Dispatchers.IO) { copyThenShutdown(first, second, prefix) }
            launch(Dispatchers.IO) { copyThenShutdown(second, first, ByteArray(0)) }
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
