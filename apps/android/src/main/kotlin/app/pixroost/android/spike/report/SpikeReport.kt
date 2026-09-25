package app.pixroost.android.spike.report

import android.os.Build
import app.pixroost.android.spike.ui.model.PanelState
import app.pixroost.android.spike.util.formatted
import kotlin.math.roundToInt

/** Plain-text report to paste into the spike's issue. */
fun buildReport(state: PanelState, refreshRateHz: Float): String = buildString {
    appendLine("Pixroost S-02")
    appendLine(
        "Устройство: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} " +
            "(API ${Build.VERSION.SDK_INT}), экран ${refreshRateHz.roundToInt()} Гц",
    )
    appendLine(accessLabel(state.access))
    state.scan?.let { appendLine(scanLabel(it)) }
    val thumbnails = if (state.options.systemThumbnails) "системные (loadThumbnail)" else "декодирование Coil"
    val cache = if (state.options.memoryCache) "да" else "нет"
    appendLine("Сетка: ${state.cells.formatted()} ячеек, превью: $thumbnails, кэш в памяти: $cache")
    state.autoScroll?.let { appendLine(autoScrollLabel(it)) }
    appendLine("С последнего сброса: ${statsLabel(state.liveStats)}")
}
