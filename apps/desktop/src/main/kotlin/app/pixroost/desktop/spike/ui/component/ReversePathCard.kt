package app.pixroost.desktop.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.report.phoneFindingLabel
import app.pixroost.desktop.spike.ui.UiConstants
import app.pixroost.desktop.spike.ui.model.PhoneFinding

/** The reversed path, PC → phone: phones the PC found and whether it could connect to them. */
@Composable
fun ReversePathCard(
    phones: List<PhoneFinding>,
    onRediscover: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Развёрнутый путь: ПК → телефон", modifier) {
        phones.ifEmpty { listOf(null) }.forEach { finding ->
            Text(finding?.let(::phoneFindingLabel) ?: "телефон пока не найден", style = style)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
            OutlinedButton(onClick = onRediscover) { Text("Искать заново") }
            if (phones.isNotEmpty()) OutlinedButton(onClick = onReconnect) { Text("Подключиться снова") }
        }
    }
}
