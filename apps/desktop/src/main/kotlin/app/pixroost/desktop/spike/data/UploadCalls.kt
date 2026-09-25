package app.pixroost.desktop.spike.data

import app.pixroost.desktop.spike.util.isSha256Hex
import io.ktor.server.application.ApplicationCall

/** The upload id from the path, or null when it is not a SHA-256 (it becomes a file name). */
fun ApplicationCall.uploadId(): String? = parameters["id"]?.takeIf(::isSha256Hex)

/** Relayed connections come from 127.0.0.1; everything else came straight from the phone. */
fun ApplicationCall.uploadPath(): UploadPath =
    if (request.local.remoteAddress == LanDataConstants.LOOPBACK) UploadPath.Reversed else UploadPath.Direct
