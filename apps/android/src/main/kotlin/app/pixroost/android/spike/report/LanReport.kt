package app.pixroost.android.spike.report

import android.os.Build
import app.pixroost.android.spike.ui.model.LanSpikeUiState

/** Plain-text report of the phone side to paste into the spike's issue. */
fun buildLanReport(state: LanSpikeUiState): String = buildString {
    appendLine("Pixroost S-04, телефон")
    appendLine("Телефон: ${Build.MANUFACTURER} ${Build.MODEL}")
    appendLine("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    state.addresses.forEach { appendLine("Адрес: $it") }
    appendLine("Сервер для ПК: ${state.serverStatus}")
    val pcs = state.pcs.map(::pcFindingLabel).ifEmpty { listOf("ПК не найден") }
    appendLine("Прямой путь (телефон → ПК): ${pcs.joinToString(" ; ")}")
    val requests = state.discoverRequests.map { "${it.host}: ${it.count} шт., первый через ${it.firstAfterMillis} мс" }
    appendLine("Запросы «кто тут?» от ПК: ${requests.ifEmpty { listOf("не было") }.joinToString(" ; ")}")
    appendLine(
        "Развёрнутый путь (ПК → телефон): ${state.pcGreetings.ifEmpty {
            listOf("подключений нет")
        }.joinToString(" ; ")}",
    )
    appendLine("Журнал:")
    state.log.forEach { appendLine("  ${it.elapsedMillis} мс  ${it.text}") }
}
