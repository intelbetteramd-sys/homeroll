package app.pixroost.android.spike.ui.component

import android.os.Build
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.GridOptions

/** Switches between the grid settings the spike compares. */
@Composable
fun GridOptionChips(options: GridOptions, onOptions: (GridOptions) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(UiConstants.PANEL_SPACING),
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FilterChip(
                selected = options.systemThumbnails,
                onClick = { onOptions(options.copy(systemThumbnails = !options.systemThumbnails)) },
                label = { Text("Системные превью") },
            )
        }
        FilterChip(
            selected = options.memoryCache,
            onClick = { onOptions(options.copy(memoryCache = !options.memoryCache)) },
            label = { Text("Кэш в памяти") },
        )
        UiConstants.GRID_TARGETS.forEach { (target, label) ->
            FilterChip(
                selected = options.target == target,
                onClick = { onOptions(options.copy(target = target)) },
                label = { Text(label) },
            )
        }
    }
}
