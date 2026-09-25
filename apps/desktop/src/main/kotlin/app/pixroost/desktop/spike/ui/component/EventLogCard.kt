package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.ui.model.LogLine

@Composable
fun EventLogCard(log: List<LogLine>, modifier: Modifier = Modifier) {
    SectionCard("Журнал", modifier) {
        log.asReversed().forEach {
            Text("${it.elapsedMillis} мс  ${it.text}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
