package app.pixroost.desktop.spike.report

import app.pixroost.desktop.spike.ui.model.OAuthSpikeUiState

/** Plain-text report of the PC side to paste into the spike's issue. It holds no tokens and no account names. */
fun buildOAuthReport(state: OAuthSpikeUiState, pickerStatus: String): String = buildString {
    appendLine("Pixroost S-05, ПК")
    appendLine("ОС: ${System.getProperty("os.name")} ${System.getProperty("os.version")}")
    appendLine("Хранение: ${state.tokenStorage}")
    state.services.values.forEach { service ->
        appendLine("${service.service.label}: ${service.status}")
        service.tokenInfo?.let { appendLine("  $it") }
        service.account?.let { appendLine("  $it") }
    }
    appendLine("Выбор в Google Фото: $pickerStatus")
    appendLine("Журнал:")
    state.log.forEach { appendLine("  ${it.elapsedMillis} мс  ${it.text}") }
}
