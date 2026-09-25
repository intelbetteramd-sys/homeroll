package app.pixroost.android.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** Current access to photos and the ways to change it. */
@Composable
fun AccessRow(
    access: MediaAccess,
    onRequestAccess: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpikeConstants.PANEL_SPACING),
    ) {
        Text(accessLabel(access), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        if (access != MediaAccess.Full) {
            OutlinedButton(onClick = onRequestAccess) {
                Text(if (access == MediaAccess.Partial) "Выбрать ещё" else "Дать доступ")
            }
        }
        OutlinedButton(onClick = onOpenSettings) { Text("Настройки") }
    }
}
