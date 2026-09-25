package app.pixroost.desktop.spike.report

import app.pixroost.desktop.spike.ui.model.LanEvent
import app.pixroost.desktop.spike.ui.model.LanSpikeUiState

/** Plain-text report of the PC side to paste into the spike's issue. */
fun buildLanReport(state: LanSpikeUiState, log: List<LanEvent>): String = buildString {
    appendLine("Pixroost S-04, ПК")
    appendLine("ПК: ${state.pcName}, ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    state.addresses.forEach { appendLine("Адрес: $it") }
    appendLine("mDNS: ${state.announceStatus}; сервер: ${state.serverStatus}")
    state.firewall?.takeIf { it.isWindows }?.let { firewall ->
        appendLine("Профили сети: ${firewall.networkProfiles.joinToString()}")
        appendLine("Правила для Java: ${firewall.rules.ifEmpty { listOf("нет") }.joinToString(" ; ")}")
    }
    appendLine(
        "Прямой путь (телефон → ПК): ${state.greetings.ifEmpty {
            listOf("подключений нет")
        }.joinToString(" ; ")}",
    )
    appendLine(
        "Развёрнутый путь (ПК → телефон): " +
            state.phones.ifEmpty {
                null
            }?.joinToString(" ; ") { phoneFindingLabel(it) }.orEmpty().ifEmpty { "телефон не найден" },
    )
    appendLine("Журнал:")
    log.forEach { appendLine("  ${it.elapsedMillis} мс  ${it.text}") }
}
