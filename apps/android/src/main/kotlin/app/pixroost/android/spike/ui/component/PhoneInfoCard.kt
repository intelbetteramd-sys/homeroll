package app.pixroost.android.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** This phone: name, addresses and the server that waits for the PC. */
@Composable
fun PhoneInfoCard(phoneName: String, addresses: List<String>, serverStatus: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Этот телефон: $phoneName", modifier) {
        addresses.ifEmpty { listOf("нет адреса в локальной сети — Wi-Fi включён?") }.forEach { Text(it, style = style) }
        Text("Сервер для ПК: $serverStatus", style = style)
    }
}
