package app.pixroost.android.spike.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.data.FileStatus
import app.pixroost.android.spike.data.FileTransfer
import app.pixroost.android.spike.report.batchSummary
import app.pixroost.android.spike.report.transferLabel

/** Start or stop, the overall progress, totals and the files that are not done yet. */
@Composable
fun TransferCard(
    transfers: List<FileTransfer>,
    isRunning: Boolean,
    canStart: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard("Отправка", modifier) {
        if (isRunning) {
            OutlinedButton(onClick = onStop) { Text("Остановить") }
        } else {
            Button(onClick = onStart, enabled = canStart) { Text("Отправить на ПК") }
        }
        val total = transfers.sumOf { it.file.size }
        if (total > 0) LinearProgressIndicator(progress = { transfers.sumOf { it.sent }.toFloat() / total })
        batchSummary(transfers).forEach { Text(it, style = MaterialTheme.typography.titleSmall) }
        transfers.filter { it.status != FileStatus.Done && it.status != FileStatus.Queued }.forEach {
            Text(transferLabel(it), style = MaterialTheme.typography.bodySmall)
        }
    }
}
