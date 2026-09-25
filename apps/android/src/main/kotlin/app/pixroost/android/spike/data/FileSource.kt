package app.pixroost.android.spike.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

/** Opens test files and picked photos at any offset. Blocking: call on Dispatchers.IO. */
class FileSource(private val context: Context) {
    fun open(file: SpikeFile, offset: Long = 0): InputStream = if (file.location.startsWith("content:")) {
        val input = context.contentResolver.openInputStream(Uri.parse(file.location))
            ?: throw IOException("не открылся ${file.name}")
        input.also { it.skipFully(offset) }
    } else {
        FileInputStream(File(file.location)).also { it.channel.position(offset) }
    }

    /** Name and size of a picked photo or video. */
    fun describe(uri: Uri): SpikeFile? = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
        null,
        null,
        null,
    )?.use { cursor ->
        if (!cursor.moveToFirst()) return null
        SpikeFile(
            name = cursor.getString(0) ?: uri.lastPathSegment.orEmpty(),
            size = cursor.getLong(1),
            location = "$uri",
        )
    }

    private fun InputStream.skipFully(count: Long) {
        var left = count
        while (left > 0) {
            val skipped = skip(left)
            if (skipped <= 0) {
                if (read() < 0) throw IOException("файл короче, чем ожидалось")
                left--
            } else {
                left -= skipped
            }
        }
    }
}
