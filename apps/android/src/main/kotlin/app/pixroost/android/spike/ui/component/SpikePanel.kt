package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.report.autoScrollLabel
import app.pixroost.android.spike.report.gridLabel
import app.pixroost.android.spike.report.scanLabel
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.GridOptions
import app.pixroost.android.spike.ui.model.PanelActions
import app.pixroost.android.spike.ui.model.PanelState

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
                horizontal = UiConstants.PANEL_PADDING_HORIZONTAL,
                vertical = UiConstants.PANEL_PADDING_VERTICAL,
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
