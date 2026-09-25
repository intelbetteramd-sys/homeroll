package app.pixroost.android.spike.util

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.withFrameNanos
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

/** Scrolls at a constant speed for [duration] or until the end, like a long fast swipe. */
suspend fun LazyGridState.autoScroll(pxPerSecond: Float, duration: Duration) {
    scroll {
        val start = withFrameNanos { it }
        var last = start
        while ((last - start).nanoseconds < duration) {
            val now = withFrameNanos { it }
            val delta = pxPerSecond * (now - last).nanoseconds.toDouble(DurationUnit.SECONDS).toFloat()
            last = now
            if (delta > 0f && scrollBy(delta) == 0f) break
        }
    }
}
