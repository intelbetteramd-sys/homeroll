package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.ui.UiConstants

@Composable
fun MeasureButtons(
    onAutoScroll: () -> Unit,
    onReset: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(UiConstants.PANEL_SPACING)) {
        Button(onClick = onAutoScroll) { Text("Автопрокрутка") }
        OutlinedButton(onClick = onReset) { Text("Сброс") }
        OutlinedButton(onClick = onReport) { Text("Отчёт") }
    }
}
