package app.pixroost.android.spike.data

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
