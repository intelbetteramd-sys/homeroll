package app.pixroost.android.spike.ui.model

import app.pixroost.android.spike.data.MediaAccess
import app.pixroost.android.spike.data.ScanResult
import app.pixroost.android.spike.metrics.FrameStats

/** Everything the panel shows; also the content of the report. */
data class PanelState(
    val access: MediaAccess,
    val scan: ScanResult?,
    val cells: Int,
    val options: GridOptions,
    val liveStats: FrameStats,
    val autoScroll: AutoScrollResult?,
)
