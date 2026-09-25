package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.data.SpikeFile
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.util.formatMegabytes

/** What to send: generated test files (20 photos of 3 MB and a 500 MB video) or picked in the gallery. */
@Composable
fun FilesCard(
    files: List<SpikeFile>,
    isPreparing: Boolean,
    onCreateTestFiles: () -> Unit,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard("Файлы", modifier) {
        Text(
            when {
                isPreparing -> "создаю тестовые файлы…"
                files.isEmpty() -> "не выбраны"
                else -> "${files.size} шт., ${formatMegabytes(files.sumOf { it.size })}"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
            OutlinedButton(onClick = onCreateTestFiles, enabled = !isPreparing) { Text("Тестовые") }
            OutlinedButton(onClick = onPick, enabled = !isPreparing) { Text("Из галереи") }
        }
    }
}
