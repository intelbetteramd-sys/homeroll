package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

/** Routes of the upload protocol, see TransferSpikeConstants. */
fun Application.uploadModule(handler: UploadHandler) {
    routing {
        get(TransferSpikeConstants.INFO_PATH) { handler.info(call) }
        post(TransferSpikeConstants.UPLOADS_PATH) { handler.create(call) }
        head("${TransferSpikeConstants.UPLOADS_PATH}/{id}") { handler.head(call) }
        patch("${TransferSpikeConstants.UPLOADS_PATH}/{id}") { handler.patch(call) }
    }
}
