package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.report.pcFindingLabel
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.PcFinding

/** The direct path, phone → PC: PCs found through mDNS and whether the phone could connect. */
@Composable
fun DirectPathCard(
    pcs: List<PcFinding>,
    onRediscover: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Прямой путь: телефон → ПК", modifier) {
        if (pcs.isEmpty()) {
            Text("ищу ПК…", style = style)
        } else {
            pcs.forEach { Text(pcFindingLabel(it), style = style) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
            OutlinedButton(onClick = onRediscover) { Text("Искать заново") }
            if (pcs.isNotEmpty()) OutlinedButton(onClick = onReconnect) { Text("Подключиться снова") }
        }
    }
}
