package app.pixroost.android.spike

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.ImageLoader

/** Spike S-02: the gallery as a grid of 10 000+ thumbnails with frame statistics. */
@Composable
fun SpikeScreen(meter: JankMeter, imageLoader: ImageLoader, resumeCount: Int, refreshRateHz: Float) {
    val context = LocalContext.current
    var access by remember { mutableStateOf(context.mediaAccess()) }
    var scan by remember { mutableStateOf<ScanResult?>(null) }
    var options by remember { mutableStateOf(GridOptions.initial()) }
    var liveStats by remember { mutableStateOf(meter.snapshot()) }
    var autoScroll by remember { mutableStateOf<AutoScrollResult?>(null) }
    var autoScrollRequest by remember { mutableIntStateOf(0) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        access = context.mediaAccess()
    }
    val cells = remember(scan, options.target) { expandToTarget(scan?.items.orEmpty(), options.target) }
    val gridState = rememberLazyGridState()

    MediaScanEffect(access, resumeCount, onAccess = { access = it }, onScan = { scan = it })
    FrameStatsEffect(meter) { liveStats = it }
    AutoScrollEffect(autoScrollRequest, gridState, meter) { autoScroll = it }

    val panelState = PanelState(access, scan, cells.size, options, liveStats, autoScroll)
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        SpikePanel(
            state = panelState,
            onOptions = { options = it },
            actions = PanelActions(
                requestAccess = { permissions.launch(mediaPermissions()) },
                openSettings = { context.openAppSettings() },
                resetStats = { meter.reset() },
                startAutoScroll = { autoScrollRequest++ },
                copyReport = { context.copyReport(buildReport(panelState, refreshRateHz)) },
            ),
        )
        MediaGrid(cells, options, imageLoader, gridState, Modifier.fillMaxSize())
    }
}
