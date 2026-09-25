package app.pixroost.android.spike.data

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class MediaItem(val id: Long, val isVideo: Boolean, val takenAtMillis: Long) {
    val uri: Uri
        get() = ContentUris.withAppendedId(
            if (isVideo) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            id,
        )
}
