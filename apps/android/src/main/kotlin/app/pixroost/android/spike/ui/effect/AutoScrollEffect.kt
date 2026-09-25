package app.pixroost.android.spike.ui.effect

import android.os.SystemClock
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import app.pixroost.android.spike.metrics.JankMeter
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.AutoScrollResult
import app.pixroost.android.spike.util.autoScroll
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/** Each new [request] scrolls the grid from the top for a fixed time and measures the frames. */
@Composable
fun AutoScrollEffect(request: Int, state: LazyGridState, meter: JankMeter, onResult: (AutoScrollResult) -> Unit) {
    val pxPerSecond = UiConstants.AUTO_SCROLL_DP_PER_SECOND * LocalDensity.current.density
    val currentOnResult by rememberUpdatedState(onResult)
    LaunchedEffect(request) {
        if (request == 0) return@LaunchedEffect
        state.scrollToItem(0)
        meter.reset()
        val start = SystemClock.elapsedRealtime()
        state.autoScroll(pxPerSecond, UiConstants.AUTO_SCROLL_DURATION)
        currentOnResult(
            AutoScrollResult(
                seconds = (SystemClock.elapsedRealtime() - start).milliseconds.toDouble(DurationUnit.SECONDS).toFloat(),
                itemsPassed = state.firstVisibleItemIndex,
                stats = meter.snapshot(),
            ),
        )
    }
}
