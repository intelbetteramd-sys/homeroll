package app.pixroost.android.spike

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Measurements and controls above the grid. */
@Composable
fun SpikePanel(
    state: PanelState,
    onOptions: (GridOptions) -> Unit,
    actions: PanelActions,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodySmall
    Column(
        modifier
            .fillMaxWidth()
            .padding(
                horizontal = SpikeConstants.PANEL_PADDING_HORIZONTAL,
                vertical = SpikeConstants.PANEL_PADDING_VERTICAL,
            ),
    ) {
        AccessRow(state.access, onRequestAccess = actions.requestAccess, onOpenSettings = actions.openSettings)
        state.scan?.let { Text(scanLabel(it), style = style) }
        Text(gridLabel(state.cells, state.liveStats), style = style)
        state.autoScroll?.let { Text(autoScrollLabel(it), style = style) }
        GridOptionChips(state.options, onOptions)
        MeasureButtons(
            onAutoScroll = actions.startAutoScroll,
            onReset = actions.resetStats,
            onReport = actions.copyReport,
        )
    }
}
