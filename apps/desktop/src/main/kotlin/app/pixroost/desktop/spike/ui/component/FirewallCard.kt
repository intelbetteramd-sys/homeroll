package app.pixroost.desktop.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.data.FirewallState
import app.pixroost.desktop.spike.ui.UiConstants

/**
 * Network profiles and the firewall rules for this Java executable, with a test rule to add or remove.
 * The buttons run PowerShell, so they are shown on Windows only.
 */
@Composable
fun FirewallCard(
    firewall: FirewallState?,
    onRefresh: () -> Unit,
    onAddRule: () -> Unit,
    onRemoveRule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Брандмауэр Windows", modifier) {
        when {
            firewall == null -> Text("проверяю…", style = style)

            !firewall.isWindows -> Text(
                "Не Windows. На Mac: Системные настройки → Сеть → Брандмауэр, по умолчанию он выключен.",
                style = style,
            )

            else -> {
                Text("Профили сети:", style = style)
                firewall.networkProfiles.ifEmpty {
                    listOf("не удалось прочитать")
                }.forEach { Text("  $it", style = style) }
                Text("Правила для ${firewall.javaPath}:", style = style)
                firewall.rules.ifEmpty { listOf("правил нет") }.forEach { Text("  $it", style = style) }
            }
        }
        if (firewall?.isWindows == true) {
            Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
                OutlinedButton(onClick = onRefresh) { Text("Обновить") }
                OutlinedButton(onClick = onAddRule) { Text("Добавить правило, как установщик") }
                OutlinedButton(onClick = onRemoveRule) { Text("Удалить все правила Java") }
            }
        }
    }
}
