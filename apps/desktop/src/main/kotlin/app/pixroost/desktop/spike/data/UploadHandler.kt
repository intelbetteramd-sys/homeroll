package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import app.pixroost.desktop.spike.util.formatMegabytes
import app.pixroost.desktop.spike.util.formatSpeed
import app.pixroost.desktop.spike.util.isSha256Hex
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap

/**
 * The upload protocol on the PC: create an upload, ask how much arrived, send the rest. A new attempt for the
 * same file cancels the previous one, which may still hang on a connection that died with the Wi-Fi.
 */
class UploadHandler(
    private val store: TransferStore,
    private val pcName: String,
    private val onEvent: (String) -> Unit,
) {
    private val _uploads = MutableStateFlow<Map<String, ReceivedUpload>>(emptyMap())
    val uploads: StateFlow<Map<String, ReceivedUpload>> = _uploads.asStateFlow()
    private val attempts = ConcurrentHashMap<String, Job>()

    suspend fun info(call: ApplicationCall) = call.respondText("name=$pcName\n")

    suspend fun create(call: ApplicationCall) {
        val id = call.request.header(TransferSpikeConstants.HEADER_SHA256)?.lowercase()?.takeIf(::isSha256Hex)
        val length = call.request.header(TransferSpikeConstants.HEADER_LENGTH)?.toLongOrNull()?.takeIf { it >= 0 }
        if (id == null || length == null) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }
        val name = call.request.header(TransferSpikeConstants.HEADER_NAME)
            ?.let { URLDecoder.decode(it, Charsets.UTF_8) } ?: id
        val isArchived = store.archivedFile(id) != null
        val offset = if (isArchived) length else withContext(Dispatchers.IO) { store.offset(id) }
        _uploads.update { uploads ->
            val known = uploads[id] ?: ReceivedUpload(id, name, length, offset, call.uploadPath(), UploadStatus.Waiting)
            uploads + (id to if (isArchived) known.copy(status = UploadStatus.Duplicate) else known)
        }
        call.response.header(TransferSpikeConstants.HEADER_OFFSET, offset)
        if (isArchived) {
            onEvent("«$name» уже в архиве, передавать не нужно")
            call.response.header(TransferSpikeConstants.HEADER_COMPLETE, "1")
            call.respond(HttpStatusCode.OK)
        } else {
            call.response.header(HttpHeaders.Location, "${TransferSpikeConstants.UPLOADS_PATH}/$id")
            call.respond(HttpStatusCode.Created)
        }
    }

    suspend fun head(call: ApplicationCall) {
        val upload = call.uploadId()?.let { _uploads.value[it] }
        if (upload == null) {
            call.respond(HttpStatusCode.NotFound)
            return
        }
        val offset = if (store.archivedFile(upload.id) != null) {
            upload.length
        } else {
            withContext(Dispatchers.IO) { store.offset(upload.id) }
        }
        call.response.header(TransferSpikeConstants.HEADER_OFFSET, offset)
        call.response.header(TransferSpikeConstants.HEADER_LENGTH, upload.length)
        call.respond(HttpStatusCode.OK)
    }

    suspend fun patch(call: ApplicationCall) {
        val upload = call.uploadId()?.let { _uploads.value[it] }
        val clientOffset = call.request.header(TransferSpikeConstants.HEADER_OFFSET)?.toLongOrNull()
        if (upload == null || clientOffset == null) {
            call.respond(if (upload == null) HttpStatusCode.NotFound else HttpStatusCode.BadRequest)
            return
        }
        attempts.remove(upload.id)?.cancelAndJoin()
        val job = currentCoroutineContext().job
        attempts[upload.id] = job
        try {
            val offset = withContext(Dispatchers.IO) { store.offset(upload.id) }
            if (clientOffset == offset) {
                receive(call, upload, offset)
            } else {
                call.response.header(TransferSpikeConstants.HEADER_OFFSET, offset)
                call.respond(HttpStatusCode.Conflict)
            }
        } finally {
            attempts.remove(upload.id, job)
        }
    }

    private suspend fun receive(call: ApplicationCall, upload: ReceivedUpload, offset: Long) {
        val path = call.uploadPath()
        val now = System.currentTimeMillis()
        change(upload.id) {
            it.copy(
                received = offset,
                path = path,
                status = UploadStatus.Receiving,
                attempts = it.attempts + 1,
                attemptStartOffset = offset,
                attemptStartedAt = now,
                firstStartedAt = if (it.attempts == 0) now else it.firstStartedAt,
            )
        }
        onEvent(
            if (offset == 0L) {
                "Приём «${upload.name}», ${formatMegabytes(upload.length)}, путь ${path.label}"
            } else {
                "Докачка «${upload.name}» с ${formatMegabytes(offset)}, путь ${path.label}"
            },
        )
        val received = try {
            copyBody(call, upload, offset)
        } catch (error: IOException) {
            val kept = withContext(Dispatchers.IO) { store.offset(upload.id) }
            change(upload.id) { it.copy(received = kept, status = UploadStatus.Interrupted) }
            onEvent("«${upload.name}» оборвалась на ${formatMegabytes(kept)}: ${error.message}")
            return
        }
        if (received == upload.length) {
            finish(call, upload)
        } else {
            change(upload.id) { it.copy(received = received, status = UploadStatus.Waiting) }
            call.response.header(TransferSpikeConstants.HEADER_OFFSET, received)
            call.respond(HttpStatusCode.NoContent)
        }
    }

    /** Appends the request body to the part file; returns the new offset. */
    private suspend fun copyBody(call: ApplicationCall, upload: ReceivedUpload, offset: Long): Long {
        val channel = call.receiveChannel()
        val buffer = ByteArray(TransferSpikeConstants.COPY_BUFFER_SIZE)
        return withContext(Dispatchers.IO) {
            var received = offset
            var reported = offset
            FileOutputStream(store.partFile(upload.id), true).use { output ->
                while (true) {
                    val read = channel.readAvailable(buffer, 0, buffer.size)
                    if (read < 0) break
                    if (received + read > upload.length) throw IOException("телефон прислал больше, чем объявил")
                    output.write(buffer, 0, read)
                    received += read
                    if (received - reported >= LanDataConstants.PROGRESS_STEP_BYTES) {
                        reported = received
                        change(upload.id) { it.copy(received = received) }
                    }
                }
            }
            received
        }
    }

    private suspend fun finish(call: ApplicationCall, upload: ReceivedUpload) {
        change(upload.id) { it.copy(received = upload.length, status = UploadStatus.Verifying) }
        val verifyStartedAt = System.currentTimeMillis()
        val saved = withContext(Dispatchers.IO) { store.complete(upload.id, upload.name) }
        val finishedAt = System.currentTimeMillis()
        val done = change(upload.id) {
            it.copy(
                status = if (saved == null) UploadStatus.Mismatch else UploadStatus.Done,
                finishedAt = finishedAt,
                verifyMillis = finishedAt - verifyStartedAt,
                savedAs = saved?.name,
            )
        }
        if (saved == null) {
            onEvent("«${upload.name}»: SHA-256 не совпал, файл отложен как .bad")
            call.respond(HttpStatusCode.UnprocessableEntity)
            return
        }
        val attemptBytes = upload.length - done.attemptStartOffset
        onEvent(
            "Готово «${upload.name}»: ${formatSpeed(attemptBytes, verifyStartedAt - done.attemptStartedAt)}, " +
                "попыток ${done.attempts}, SHA-256 совпал (${done.verifyMillis} мс)",
        )
        call.response.header(TransferSpikeConstants.HEADER_OFFSET, upload.length)
        call.response.header(TransferSpikeConstants.HEADER_COMPLETE, "1")
        call.respond(HttpStatusCode.NoContent)
    }

    private fun change(id: String, transform: (ReceivedUpload) -> ReceivedUpload): ReceivedUpload {
        _uploads.update { uploads -> uploads[id]?.let { uploads + (id to transform(it)) } ?: uploads }
        return _uploads.value.getValue(id)
    }
}
