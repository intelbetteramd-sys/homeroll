package app.pixroost.android.spike.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import app.pixroost.android.spike.metrics.FrameStats
import app.pixroost.android.spike.metrics.JankMeter
import app.pixroost.android.spike.ui.UiConstants
import kotlinx.coroutines.delay

/** Reports frame statistics twice a second, so the panel does not recompose on every frame. */
@Composable
fun FrameStatsEffect(meter: JankMeter, onStats: (FrameStats) -> Unit) {
    val currentOnStats by rememberUpdatedState(onStats)
    LaunchedEffect(meter) {
        while (true) {
            delay(UiConstants.STATS_REFRESH_INTERVAL)
            currentOnStats(meter.snapshot())
        }
    }
}
