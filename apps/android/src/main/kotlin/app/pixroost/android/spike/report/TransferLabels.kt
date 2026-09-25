package app.pixroost.android.spike.report

import app.pixroost.android.spike.data.FileStatus
import app.pixroost.android.spike.data.FileTransfer
import app.pixroost.android.spike.data.LanDataConstants
import app.pixroost.android.spike.util.formatMegabytes
import app.pixroost.android.spike.util.formatSpeed

/** "test-video.mp4: 120.0 из 500.0 МБ, отправляется; путь прямой, попыток 2, докачка с 80.0 МБ, 31.2 МБ/с". */
fun transferLabel(transfer: FileTransfer): String {
    val end = transfer.finishedAt ?: System.currentTimeMillis()
    val details = buildList {
        transfer.route?.let { add("путь ${it.label}") }
        if (transfer.attempts > 0) add("попыток ${transfer.attempts}")
        transfer.resumedFrom.forEach { add("докачка с ${formatMegabytes(it)}") }
        if (transfer.attempts > 0) {
            val speed = formatSpeed(transfer.sent - transfer.attemptStartOffset, end - transfer.attemptStartedAt)
            add("последняя попытка $speed")
        }
        transfer.firstAttemptAt?.let { first -> transfer.finishedAt?.let { add("всего ${it - first} мс") } }
        transfer.hashMillis?.let { add("SHA-256 за $it мс") }
        transfer.error?.let { add(it) }
    }
    return "${transfer.file.name}: ${formatMegabytes(transfer.sent)} из ${formatMegabytes(transfer.file.size)}, " +
        "${transfer.status.label}" + if (details.isEmpty()) "" else "; ${details.joinToString(", ")}"
}

/** Photos and large files separately: all bytes over the time from the first request to the last finish. */
fun batchSummary(transfers: List<FileTransfer>): List<String> = transfers
    .filter { it.status == FileStatus.Done }
    .groupBy { it.file.size >= LanDataConstants.LARGE_FILE_BYTES }
    .map { (isLarge, done) ->
        val bytes = done.sumOf { it.file.size }
        val millis = done.maxOf { it.finishedAt ?: 0 } - done.minOf { it.firstAttemptAt ?: 0 }
        "${if (isLarge) "Большие" else "Фото"}: файлов ${done.size}, ${formatMegabytes(bytes)} за $millis мс, " +
            "${formatSpeed(bytes, millis)}, попыток ${done.sumOf { it.attempts }}"
    }
