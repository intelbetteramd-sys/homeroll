package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** The direct path, phone → PC: connections the phone opened to this PC. */
@Composable
fun DirectPathCard(greetings: List<String>, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("Прямой путь: телефон → ПК", modifier) {
        greetings.ifEmpty { listOf("телефон ещё не подключался") }.forEach { Text(it, style = style) }
    }
}
