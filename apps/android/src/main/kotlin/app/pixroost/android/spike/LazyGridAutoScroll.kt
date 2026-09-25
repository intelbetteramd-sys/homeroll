package app.pixroost.android.spike

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.withFrameNanos

/** Scrolls at a constant speed for [durationMillis] or until the end, like a long fast swipe. */
suspend fun LazyGridState.autoScroll(pxPerSecond: Float, durationMillis: Long) {
    scroll {
        val start = withFrameNanos { it }
        var last = start
        while (last - start < durationMillis * SpikeConstants.NANOS_PER_MILLI) {
            val now = withFrameNanos { it }
            val delta = pxPerSecond * (now - last) / SpikeConstants.NANOS_PER_SECOND
            last = now
            if (delta > 0f && scrollBy(delta) == 0f) break
        }
    }
}
