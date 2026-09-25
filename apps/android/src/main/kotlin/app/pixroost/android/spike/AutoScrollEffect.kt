package app.pixroost.android.spike

import android.os.SystemClock
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity

/** Each new [request] scrolls the grid from the top for a fixed time and measures the frames. */
@Composable
fun AutoScrollEffect(request: Int, state: LazyGridState, meter: JankMeter, onResult: (AutoScrollResult) -> Unit) {
    val pxPerSecond = SpikeConstants.AUTO_SCROLL_DP_PER_SECOND * LocalDensity.current.density
    val currentOnResult by rememberUpdatedState(onResult)
    LaunchedEffect(request) {
        if (request == 0) return@LaunchedEffect
        state.scrollToItem(0)
        meter.reset()
        val start = SystemClock.elapsedRealtime()
        state.autoScroll(pxPerSecond, SpikeConstants.AUTO_SCROLL_MILLIS)
        currentOnResult(
            AutoScrollResult(
                seconds = (SystemClock.elapsedRealtime() - start).toFloat() / SpikeConstants.MILLIS_PER_SECOND,
                itemsPassed = state.firstVisibleItemIndex,
                stats = meter.snapshot(),
            ),
        )
    }
}
