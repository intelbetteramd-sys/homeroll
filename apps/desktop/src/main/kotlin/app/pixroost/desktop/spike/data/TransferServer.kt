package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty

/** The PC's HTTPS server on all addresses: the phone connects to it directly or through the reversed relay. */
class TransferServer(certificate: ServerCertificate, handler: UploadHandler) {
    private val server = embeddedServer(
        Netty,
        configure = {
            sslConnector(
                keyStore = certificate.keyStore,
                keyAlias = LanDataConstants.KEY_ALIAS,
                keyStorePassword = { LanDataConstants.KEYSTORE_PASSWORD.toCharArray() },
                privateKeyPassword = { LanDataConstants.KEYSTORE_PASSWORD.toCharArray() },
            ) {
                host = "0.0.0.0"
                port = LanSpikeConstants.PC_PORT
            }
            requestReadTimeoutSeconds = LanDataConstants.REQUEST_READ_TIMEOUT_SECONDS
        },
        module = { uploadModule(handler) },
    )

    fun start() {
        server.start(wait = false)
    }

    fun stop() {
        server.stop(gracePeriodMillis = 0, timeoutMillis = LanDataConstants.STOP_TIMEOUT_MILLIS)
    }
}
