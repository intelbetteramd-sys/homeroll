package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.data.FoundPhone
import app.pixroost.desktop.spike.data.ReverseLinkStats
import app.pixroost.desktop.spike.report.phoneLabel

/** Phones that answered the broadcast and the reversed connections the PC keeps open to them. */
@Composable
fun PhonesCard(
    phones: List<FoundPhone>,
    links: Map<String, ReverseLinkStats>,
    onRediscover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Развёрнутый путь: телефоны", modifier) {
        if (phones.isEmpty()) Text("телефон пока не найден", style = style)
        phones.forEach { Text(phoneLabel(it, links[it.host]), style = style) }
        OutlinedButton(onClick = onRediscover) { Text("Искать заново") }
    }
}
