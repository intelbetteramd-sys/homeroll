package app.pixroost.android.spike.metrics

/** Frame statistics since the last reset. */
data class FrameStats(
    val frames: Int,
    val janky: Int,
    val p50Millis: Float,
    val p90Millis: Float,
    val p99Millis: Float,
    val maxMillis: Float,
) {
    val jankyPercent: Float get() = if (frames == 0) 0f else janky * MetricsConstants.PERCENT / frames
}
