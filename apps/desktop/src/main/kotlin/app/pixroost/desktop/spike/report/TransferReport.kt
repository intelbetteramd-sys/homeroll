package app.pixroost.desktop.spike.report

import app.pixroost.desktop.spike.data.ReceivedUpload
import app.pixroost.desktop.spike.data.ReverseLinkStats
import app.pixroost.desktop.spike.ui.model.LanEvent
import app.pixroost.desktop.spike.ui.model.TransferSpikeUiState
import app.pixroost.desktop.spike.util.formatFingerprint

/** Plain-text report of the PC side to paste into the spike's issue. */
fun buildTransferReport(
    state: TransferSpikeUiState,
    uploads: Collection<ReceivedUpload>,
    links: Map<String, ReverseLinkStats>,
    log: List<LanEvent>,
): String = buildString {
    appendLine("Pixroost S-03, ПК")
    appendLine("ПК: ${state.pcName}, ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    state.addresses.forEach { appendLine("Адрес: $it") }
    appendLine("Сервер: ${state.serverStatus}; сертификат ${formatFingerprint(state.fingerprint)}")
    state.firewall?.takeIf { it.isWindows }?.let { firewall ->
        appendLine("Профили сети: ${firewall.networkProfiles.joinToString()}")
        appendLine("Правила для Java: ${firewall.rules.ifEmpty { listOf("нет") }.joinToString(" ; ")}")
    }
    state.phones.forEach { appendLine("Телефон: ${phoneLabel(it, links[it.host])}") }
    appendLine("Итоги:")
    batchSummary(uploads).ifEmpty { listOf("готовых файлов нет") }.forEach { appendLine("  $it") }
    appendLine("Файлы:")
    uploads.forEach { appendLine("  ${uploadLabel(it)}") }
    appendLine("Журнал:")
    log.forEach { appendLine("  ${it.elapsedMillis} мс  ${it.text}") }
}
