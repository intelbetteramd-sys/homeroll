package app.pixroost.android.spike.report

import android.os.Build
import app.pixroost.android.spike.data.FileTransfer
import app.pixroost.android.spike.ui.model.TransferSpikeUiState
import app.pixroost.android.spike.util.formatFingerprint

/** Plain-text report of the phone side to paste into the spike's issue. */
fun buildTransferReport(state: TransferSpikeUiState, transfers: List<FileTransfer>): String = buildString {
    appendLine("Pixroost S-03, телефон")
    appendLine("Телефон: ${state.phoneName}, Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    state.addresses.forEach { appendLine("Адрес: $it") }
    appendLine("ПК: ${state.pc?.let { "${it.name} (${it.host}), найден за ${it.foundAfterMillis} мс" } ?: "не найден"}")
    appendLine("Сертификат ПК: ${state.pinned?.let(::formatFingerprint) ?: "не запомнен"}")
    appendLine("Путь: ${state.route.label}")
    appendLine("Итоги:")
    batchSummary(transfers).ifEmpty { listOf("готовых файлов нет") }.forEach { appendLine("  $it") }
    appendLine("Файлы:")
    transfers.forEach { appendLine("  ${transferLabel(it)}") }
    appendLine("Журнал:")
    state.log.forEach { appendLine("  ${it.elapsedMillis} мс  ${it.text}") }
}
