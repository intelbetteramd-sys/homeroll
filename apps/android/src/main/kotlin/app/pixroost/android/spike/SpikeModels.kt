package app.pixroost.android.spike

import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

enum class MediaAccess { Full, Partial, None }

data class MediaItem(val id: Long, val isVideo: Boolean, val takenAtMillis: Long) {
    val uri: Uri
        get() = ContentUris.withAppendedId(
            if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id,
        )
}

/** What a full scan of MediaStore costs and what changed since the previous scan. */
data class ScanResult(
    val items: List<MediaItem>,
    val queryMillis: Long,
    val readMillis: Long,
    val firstPageMillis: Long,
    val generation: Long?,
    val changedSinceLastScan: Int?,
    val storeVersionChanged: Boolean,
)

/** A request for the thumbnail MediaStore already keeps on disk, instead of decoding the original. */
data class SystemThumbnail(val uri: Uri)

/** Frame statistics since the last reset. */
data class FrameStats(
    val frames: Int,
    val janky: Int,
    val p50Millis: Float,
    val p90Millis: Float,
    val p99Millis: Float,
    val maxMillis: Float,
) {
    val jankyPercent: Float get() = if (frames == 0) 0f else janky * SpikeConstants.PERCENT / frames
}

/** Grid settings the spike compares. */
data class GridOptions(val systemThumbnails: Boolean, val memoryCache: Boolean, val target: Int) {
    companion object {
        fun initial() = GridOptions(
            systemThumbnails = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
            memoryCache = true,
            target = SpikeConstants.TARGET_AS_IS,
        )
    }
}

data class GridCell(val key: String, val item: MediaItem)

data class AutoScrollResult(val seconds: Float, val itemsPassed: Int, val stats: FrameStats)

/** Everything the panel shows; also the content of the report. */
data class PanelState(
    val access: MediaAccess,
    val scan: ScanResult?,
    val cells: Int,
    val options: GridOptions,
    val liveStats: FrameStats,
    val autoScroll: AutoScrollResult?,
)

class PanelActions(
    val requestAccess: () -> Unit,
    val openSettings: () -> Unit,
    val resetStats: () -> Unit,
    val startAutoScroll: () -> Unit,
    val copyReport: () -> Unit,
)
