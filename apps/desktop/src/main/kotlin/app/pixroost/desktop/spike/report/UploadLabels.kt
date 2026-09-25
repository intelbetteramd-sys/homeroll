package app.pixroost.desktop.spike.report

import app.pixroost.desktop.spike.data.FoundPhone
import app.pixroost.desktop.spike.data.ReceivedUpload
import app.pixroost.desktop.spike.data.ReverseLinkStats
import app.pixroost.desktop.spike.data.UploadStatus
import app.pixroost.desktop.spike.util.formatMegabytes
import app.pixroost.desktop.spike.util.formatSpeed

/** "IMG_1.jpg: 3.0 из 3.0 МБ, готово, SHA-256 совпал; путь прямой, попыток 1, 41.2 МБ/с, проверка 12 мс". */
fun uploadLabel(upload: ReceivedUpload): String {
    val end = upload.finishedAt?.let { it - (upload.verifyMillis ?: 0) } ?: System.currentTimeMillis()
    val speed = formatSpeed(upload.received - upload.attemptStartOffset, end - upload.attemptStartedAt)
    val details = buildList {
        add("путь ${upload.path.label}")
        add("попыток ${upload.attempts}")
        if (upload.status == UploadStatus.Receiving || upload.finishedAt != null) add("последняя попытка $speed")
        upload.finishedAt?.let { add("всего ${it - upload.firstStartedAt} мс") }
        upload.verifyMillis?.let { add("проверка SHA-256 $it мс") }
        upload.savedAs?.let { add("сохранён как «$it»") }
    }
    return "${upload.name}: ${formatMegabytes(upload.received)} из ${formatMegabytes(upload.length)}, " +
        "${upload.status.label}; ${details.joinToString(", ")}"
}

/** "Redmi Note 9 (192.168.1.10), найден за 1013 мс; соединения: ждут 2, передают 1, открыто 14, ошибок 0". */
fun phoneLabel(phone: FoundPhone, stats: ReverseLinkStats?): String {
    val links = stats ?: ReverseLinkStats()
    return "${phone.name} (${phone.host}), найден за ${phone.foundAfterMillis} мс; соединения: " +
        "ждут ${links.idle}, передают ${links.active}, открыто ${links.opened}, ошибок ${links.failed}"
}

/** Throughput of finished uploads by path: all bytes over the time from the first start to the last finish. */
fun batchSummary(uploads: Collection<ReceivedUpload>): List<String> = uploads
    .filter { it.status == UploadStatus.Done }
    .groupBy { it.path }
    .map { (path, done) ->
        val bytes = done.sumOf { it.length }
        val millis = done.maxOf { it.finishedAt ?: 0 } - done.minOf { it.firstStartedAt }
        "Путь ${path.label}: файлов ${done.size}, ${formatMegabytes(bytes)} за $millis мс, " +
            "${formatSpeed(bytes, millis)}, попыток ${done.sumOf { it.attempts }}"
    }
