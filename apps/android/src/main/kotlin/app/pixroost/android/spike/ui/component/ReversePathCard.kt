package app.pixroost.android.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.ui.model.DiscoverRequests

/** The reversed path, PC → phone: "who is there" requests from the PC and its connections to the phone. */
@Composable
fun ReversePathCard(requests: List<DiscoverRequests>, greetings: List<String>, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Развёрнутый путь: ПК → телефон", modifier) {
        if (requests.isEmpty()) Text("запросов «кто тут?» от ПК не было", style = style)
        requests.forEach {
            Text("${it.host}: запросов ${it.count}, первый через ${it.firstAfterMillis} мс", style = style)
        }
        greetings.ifEmpty { listOf("ПК ещё не подключался") }.forEach { Text(it, style = style) }
    }
}
