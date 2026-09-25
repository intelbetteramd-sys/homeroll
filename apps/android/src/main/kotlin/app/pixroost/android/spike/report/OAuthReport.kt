package app.pixroost.android.spike.report

import android.os.Build
import app.pixroost.android.spike.ui.model.OAuthSpikeUiState

/** Plain-text report of the phone side to paste into the spike's issue. It holds no tokens and no account names. */
fun buildOAuthReport(state: OAuthSpikeUiState, pickerStatus: String): String = buildString {
    appendLine("Pixroost S-05, телефон")
    appendLine("Телефон: ${Build.MANUFACTURER} ${Build.MODEL}")
    appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    state.signing.forEach(::appendLine)
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
