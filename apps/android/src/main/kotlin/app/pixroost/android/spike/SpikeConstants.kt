package app.pixroost.android.spike

import android.provider.MediaStore
import androidx.compose.ui.unit.dp

object SpikeConstants {
    const val LOG_TAG = "S02"

    // MediaStore
    const val MEDIA_SELECTION = "${MediaStore.Files.FileColumns.MEDIA_TYPE} IN (?, ?)"
    const val MEDIA_SORT = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC, ${MediaStore.Files.FileColumns._ID} DESC"
    const val FIRST_PAGE_SIZE = 200
    const val PREFS_NAME = "s02"
    const val PREF_STORE_VERSION = "version"
    const val PREF_GENERATION = "generation"
    const val NO_GENERATION = -1L

    // Thumbnails
    const val DEFAULT_THUMBNAIL_PX = 256
    const val MEMORY_CACHE_SHARE = 0.25

    // Frame statistics
    const val DEFAULT_REFRESH_RATE = 60f
    const val FRAME_WINDOW = 8192
    const val P50 = 0.5f
    const val P90 = 0.9f
    const val P99 = 0.99f
    const val PERCENT = 100f
    const val STATS_REFRESH_MILLIS = 500L

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
    const val AUTO_SCROLL_MILLIS = 15_000L

    // Units
    const val MILLIS_PER_SECOND = 1000L
    const val NANOS_PER_MILLI = 1_000_000L
    const val NANOS_PER_SECOND = 1_000_000_000f
}
