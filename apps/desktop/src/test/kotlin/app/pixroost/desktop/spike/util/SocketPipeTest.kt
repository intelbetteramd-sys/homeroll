package app.pixroost.desktop.spike.util

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import kotlinx.coroutines.runBlocking
import java.net.ServerSocket
import java.net.Socket
import kotlin.test.Test
import kotlin.test.assertTrue

class SocketPipeTest {
    /** The phone vanished with the Wi-Fi: its side stays silent, the server side closes. The relay must end. */
    @Test
    fun endsWhenOneSideClosesAndTheOtherGoesSilent() = runBlocking {
        ServerSocket(0).use { listener ->
            val silentPeer = Socket("127.0.0.1", listener.localPort)
            val phone = listener.accept()
            val closingPeer = Socket("127.0.0.1", listener.localPort)
            val server = listener.accept()
            closingPeer.close()

            val startedAt = System.currentTimeMillis()
            pipe(phone, server)
            val tookMillis = System.currentTimeMillis() - startedAt

            assertTrue(tookMillis < TransferSpikeConstants.PIPE_CLOSE_GRACE_MILLIS + GRACE_MARGIN_MILLIS)
            assertTrue(phone.isClosed && server.isClosed)
            silentPeer.close()
        }
    }

    private companion object {
        const val GRACE_MARGIN_MILLIS = 2000L
    }
}
