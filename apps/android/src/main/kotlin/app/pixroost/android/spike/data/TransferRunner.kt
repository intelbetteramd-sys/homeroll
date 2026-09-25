package app.pixroost.android.spike.data

import app.pixroost.android.spike.util.formatMegabytes
import app.pixroost.android.spike.util.sha256Hex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Sends files: photo-sized ones three at a time, large ones one by one, so each group is measured on its own.
 * A broken connection is retried every two seconds for five minutes; every retry asks the PC for its offset
 * and sends only the rest.
 */
class TransferRunner(
    private val client: UploadClient,
    private val source: FileSource,
    private val baseUrl: (UploadRoute) -> String?,
    private val onEvent: (String) -> Unit,
) {
    private val _transfers = MutableStateFlow<List<FileTransfer>>(emptyList())
    val transfers: StateFlow<List<FileTransfer>> = _transfers.asStateFlow()

    suspend fun run(files: List<SpikeFile>, route: UploadRoute) {
        _transfers.value = files.map { FileTransfer(it) }
        val (large, small) = files.partition { it.size >= LanDataConstants.LARGE_FILE_BYTES }
        val permits = Semaphore(LanDataConstants.PARALLEL_SMALL_FILES)
        coroutineScope {
            small.forEach { file -> launch { permits.withPermit { upload(file, route) } } }
        }
        large.forEach { upload(it, route) }
    }

    private suspend fun upload(file: SpikeFile, route: UploadRoute) {
        val sha256 = hash(file)
        val deadline = System.currentTimeMillis() + LanDataConstants.RETRY_WINDOW_MILLIS
        var isFinished = false
        while (!isFinished) {
            isFinished = try {
                attempt(file, sha256, route)
            } catch (error: IOException) {
                retryLater(file, error, deadline)
            } catch (error: IllegalStateException) {
                fail(file, error.message)
                true
            }
        }
    }

    /** Waits before the next attempt. True when the retry window is over and the file has failed. */
    private suspend fun retryLater(file: SpikeFile, error: IOException, deadline: Long): Boolean {
        if (System.currentTimeMillis() > deadline) {
            fail(file, "связи нет 5 минут: ${error.message}")
            return true
        }
        change(file) { it.copy(status = FileStatus.Retrying, error = error.message) }
        onEvent("${file.name}: ${error.message ?: error.javaClass.simpleName}, повтор через 2 с")
        delay(LanDataConstants.RETRY_DELAY_MILLIS)
        return false
    }

    private suspend fun hash(file: SpikeFile): String {
        change(file) { it.copy(status = FileStatus.Hashing) }
        val startedAt = System.currentTimeMillis()
        val sha256 = withContext(Dispatchers.IO) { source.open(file).use(::sha256Hex) }
        change(file) { it.copy(hashMillis = System.currentTimeMillis() - startedAt) }
        return sha256
    }

    /** One request with the rest of the file. True when the PC has the whole file. */
    private suspend fun attempt(file: SpikeFile, sha256: String, route: UploadRoute): Boolean {
        val base = baseUrl(route) ?: throw IOException("ПК ещё не найден")
        val offer = client.offer(base, file, sha256)
        return if (offer.isComplete) {
            markComplete(file)
            true
        } else {
            sendRest(base, file, sha256, route, offer.offset)
        }
    }

    /** The PC already has the whole file: sent before this run, or the answer to the last request was lost. */
    private fun markComplete(file: SpikeFile) {
        val isNew = _transfers.value.first { it.file == file }.attempts == 0
        change(file) {
            it.copy(
                status = if (isNew) FileStatus.Duplicate else FileStatus.Done,
                sent = file.size,
                finishedAt = System.currentTimeMillis(),
            )
        }
    }

    private suspend fun sendRest(
        base: String,
        file: SpikeFile,
        sha256: String,
        route: UploadRoute,
        offset: Long,
    ): Boolean {
        val now = System.currentTimeMillis()
        if (offset > 0) onEvent("${file.name}: докачка с ${formatMegabytes(offset)}")
        change(file) {
            it.copy(
                status = FileStatus.Sending,
                sent = offset,
                attempts = it.attempts + 1,
                resumedFrom = if (offset > 0) it.resumedFrom + offset else it.resumedFrom,
                route = route,
                firstAttemptAt = it.firstAttemptAt ?: now,
                attemptStartOffset = offset,
                attemptStartedAt = now,
                error = null,
            )
        }
        var sent = offset
        var reported = sent
        val result = client.send(base, file, sha256, offset) { bytes ->
            sent += bytes
            if (sent - reported >= LanDataConstants.PROGRESS_STEP_BYTES) {
                reported = sent
                change(file) { it.copy(sent = sent) }
            }
        }
        if (result.isComplete) {
            change(file) {
                it.copy(status = FileStatus.Done, sent = file.size, finishedAt = System.currentTimeMillis())
            }
        }
        return result.isComplete
    }

    private fun fail(file: SpikeFile, reason: String?) {
        change(file) { it.copy(status = FileStatus.Failed, error = reason) }
        onEvent("${file.name}: не отправлен, $reason")
    }

    private fun change(file: SpikeFile, transform: (FileTransfer) -> FileTransfer) {
        _transfers.update { transfers -> transfers.map { if (it.file == file) transform(it) else it } }
    }
}
