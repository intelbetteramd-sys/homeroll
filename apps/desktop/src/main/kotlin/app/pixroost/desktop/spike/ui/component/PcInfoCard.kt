package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.core.spike.lan.LanSpikeConstants

/** This PC: name, addresses, the mDNS announcement and the server for the phone. */
@Composable
fun PcInfoCard(
    pcName: String,
    addresses: List<String>,
    announceStatus: String,
    serverStatus: String,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Этот ПК: $pcName", modifier) {
        addresses.ifEmpty { listOf("нет адресов в локальной сети") }.forEach { Text(it, style = style) }
        Text("mDNS ${LanSpikeConstants.PC_SERVICE_TYPE}: $announceStatus", style = style)
        Text("Сервер для телефона: $serverStatus", style = style)
    }
}
