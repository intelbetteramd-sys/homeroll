package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.data.ReceivedUpload
import app.pixroost.desktop.spike.report.batchSummary
import app.pixroost.desktop.spike.report.uploadLabel

/** Totals by path and every file the phone sends, newest first. */
@Composable
fun UploadsCard(uploads: Collection<ReceivedUpload>, modifier: Modifier = Modifier) {
    SectionCard("Принятые файлы", modifier) {
        if (uploads.isEmpty()) Text("пока ничего", style = MaterialTheme.typography.bodyMedium)
        batchSummary(uploads).forEach { Text(it, style = MaterialTheme.typography.titleSmall) }
        uploads.reversed().forEach { Text(uploadLabel(it), style = MaterialTheme.typography.bodySmall) }
    }
}
