package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.ui.model.TransferSpikeUiState
import app.pixroost.desktop.spike.util.formatFingerprint

/** The PC's name, addresses, HTTPS server and the certificate fingerprint the phone should show too. */
@Composable
fun ServerCard(state: TransferSpikeUiState, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("ПК ${state.pcName}", modifier) {
        state.addresses.forEach { Text(it, style = style) }
        Text("Сервер: ${state.serverStatus}", style = style)
        Text("mDNS: ${state.announceStatus}", style = style)
        Text("Сертификат: ${formatFingerprint(state.fingerprint).ifEmpty { "…" }}", style = style)
        Text("Файлы: ${state.folder}", style = style)
    }
}
