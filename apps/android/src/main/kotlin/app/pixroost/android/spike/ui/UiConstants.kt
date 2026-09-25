package app.pixroost.android.spike.ui

import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

object UiConstants {
    val STATS_REFRESH_INTERVAL = 500.milliseconds

    // Layout
    val PANEL_PADDING_HORIZONTAL = 12.dp
    val PANEL_PADDING_VERTICAL = 8.dp
    val PANEL_SPACING = 8.dp
    val CELL_SPACING = 1.dp
    val BADGE_PADDING = 4.dp

    // Grid
    val CELL_MIN_SIZE = 96.dp
    const val CELL_CONTENT_TYPE = "media"
    const val TARGET_AS_IS = 0
    const val TARGET_10K = 10_000
    const val TARGET_50K = 50_000
    val GRID_TARGETS = listOf(TARGET_AS_IS to "Как есть", TARGET_10K to "10 000", TARGET_50K to "50 000")

    // Auto-scroll: a long fast swipe
    const val AUTO_SCROLL_DP_PER_SECOND = 2500f
    val AUTO_SCROLL_DURATION = 15.seconds
}
