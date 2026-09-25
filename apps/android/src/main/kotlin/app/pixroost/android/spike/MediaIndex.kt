package app.pixroost.android.spike

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore

/** Reads photos and videos from MediaStore and measures how long it takes. */
object MediaIndex {
    private val collection: Uri = MediaStore.Files.getContentUri("external")
    private val selectionArgs = arrayOf(
        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString(),
    )
    private val projection = arrayOf(
        MediaStore.Files.FileColumns._ID,
        MediaStore.Files.FileColumns.MEDIA_TYPE,
        MediaStore.Images.ImageColumns.DATE_TAKEN,
        MediaStore.Files.FileColumns.DATE_MODIFIED,
    )

    /** Blocking: call off the main thread. */
    fun scan(context: Context): ScanResult {
        val resolver = context.contentResolver
        val firstPageStart = SystemClock.elapsedRealtime()
        queryFirstPage(resolver)
        val firstPageMillis = SystemClock.elapsedRealtime() - firstPageStart

        val start = SystemClock.elapsedRealtime()
        val items = ArrayList<MediaItem>()
        var queryMillis = 0L
        resolver.query(
            collection,
            projection,
            SpikeConstants.MEDIA_SELECTION,
            selectionArgs,
            SpikeConstants.MEDIA_SORT,
        )?.use { cursor ->
            queryMillis = SystemClock.elapsedRealtime() - start
            val id = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val type = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val taken = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
            val modified = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)
            items.ensureCapacity(cursor.count)
            while (cursor.moveToNext()) {
                val takenAt = cursor.getLong(taken).takeIf { it > 0 }
                    ?: (cursor.getLong(modified) * SpikeConstants.MILLIS_PER_SECOND)
                items += MediaItem(
                    id = cursor.getLong(id),
                    isVideo = cursor.getInt(type) == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                    takenAtMillis = takenAt,
                )
            }
        }
        val readMillis = SystemClock.elapsedRealtime() - start - queryMillis

        val changes = generationChanges(context)
        return ScanResult(
            items = items,
            queryMillis = queryMillis,
            readMillis = readMillis,
            firstPageMillis = firstPageMillis,
            generation = changes?.generation,
            changedSinceLastScan = changes?.changed,
            storeVersionChanged = changes?.versionChanged ?: false,
        )
    }

    /** One page with LIMIT and OFFSET: what a paged read from MediaStore would cost. */
    private fun queryFirstPage(resolver: ContentResolver) {
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, SpikeConstants.MEDIA_SELECTION)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, SpikeConstants.MEDIA_SORT)
            putInt(ContentResolver.QUERY_ARG_LIMIT, SpikeConstants.FIRST_PAGE_SIZE)
            putInt(ContentResolver.QUERY_ARG_OFFSET, 0)
        }
        resolver.query(collection, projection, args, null)?.use { cursor ->
            while (cursor.moveToNext()) cursor.getLong(0)
        }
    }

    private data class Changes(val generation: Long, val changed: Int?, val versionChanged: Boolean)

    /**
     * Android 11+: items added or modified since the generation saved at the previous scan.
     * A different MediaStore version means generations were reset and only a full rescan is reliable.
     * Deleted items never show up here: they are found by comparing IDs.
     */
    private fun generationChanges(context: Context): Changes? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return null
        val volume = MediaStore.VOLUME_EXTERNAL_PRIMARY
        val version = MediaStore.getVersion(context, volume)
        val generation = MediaStore.getGeneration(context, volume)
        val prefs = context.getSharedPreferences(SpikeConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val savedVersion = prefs.getString(SpikeConstants.PREF_STORE_VERSION, null)
        val savedGeneration = prefs.getLong(SpikeConstants.PREF_GENERATION, SpikeConstants.NO_GENERATION)
        prefs.edit()
            .putString(SpikeConstants.PREF_STORE_VERSION, version)
            .putLong(SpikeConstants.PREF_GENERATION, generation)
            .apply()
        return when {
            savedVersion == null -> Changes(generation, changed = null, versionChanged = false)
            savedVersion != version -> Changes(generation, changed = null, versionChanged = true)
            else -> Changes(generation, countModifiedAfter(context.contentResolver, savedGeneration), false)
        }
    }

    private fun countModifiedAfter(resolver: ContentResolver, generation: Long): Int {
        val selection = "${SpikeConstants.MEDIA_SELECTION} AND ${MediaStore.MediaColumns.GENERATION_MODIFIED} > ?"
        val args = selectionArgs + generation.toString()
        return resolver.query(collection, arrayOf(MediaStore.Files.FileColumns._ID), selection, args, null)
            ?.use { it.count } ?: 0
    }
}
