package app.pixroost.android.spike

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay

/** Reports frame statistics twice a second, so the panel does not recompose on every frame. */
@Composable
fun FrameStatsEffect(meter: JankMeter, onStats: (FrameStats) -> Unit) {
    val currentOnStats by rememberUpdatedState(onStats)
    LaunchedEffect(meter) {
        while (true) {
            delay(SpikeConstants.STATS_REFRESH_MILLIS)
            currentOnStats(meter.snapshot())
        }
    }
}
