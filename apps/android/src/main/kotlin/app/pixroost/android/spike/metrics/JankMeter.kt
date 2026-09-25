package app.pixroost.android.spike.metrics

import android.view.Window
import androidx.metrics.performance.FrameData
import androidx.metrics.performance.JankStats
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit

/**
 * Counts rendered frames and janky ones with JankStats: a frame is janky when it takes more than twice
 * the display's frame budget. Keeps the durations of the last frames for percentiles.
 */
class JankMeter(window: Window) {
    private val lock = Any()
    private val durations = FloatArray(MetricsConstants.FRAME_WINDOW)
    private var frames = 0
    private var janky = 0
    private var maxMillis = 0f

    private val jankStats = JankStats.createAndTrack(window) { frame -> record(frame) }

    var isTracking: Boolean
        get() = jankStats.isTrackingEnabled
        set(value) {
            jankStats.isTrackingEnabled = value
        }

    private fun record(frame: FrameData) {
        val millis = frame.frameDurationUiNanos.nanoseconds.toDouble(DurationUnit.MILLISECONDS).toFloat()
        synchronized(lock) {
            durations[frames % durations.size] = millis
            frames++
            if (frame.isJank) janky++
            if (millis > maxMillis) maxMillis = millis
        }
    }

    fun reset() = synchronized(lock) {
        frames = 0
        janky = 0
        maxMillis = 0f
    }

    fun snapshot(): FrameStats = synchronized(lock) {
        val sorted = durations.copyOf(minOf(frames, durations.size)).sorted()
        fun percentile(p: Float) = if (sorted.isEmpty()) 0f else sorted[((sorted.size - 1) * p).toInt()]
        FrameStats(
            frames = frames,
            janky = janky,
            p50Millis = percentile(MetricsConstants.P50),
            p90Millis = percentile(MetricsConstants.P90),
            p99Millis = percentile(MetricsConstants.P99),
            maxMillis = maxMillis,
        )
    }
}
