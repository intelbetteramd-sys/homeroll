package app.pixroost.android.spike.image

import android.net.Uri

/** A request for the thumbnail MediaStore already keeps on disk, instead of decoding the original. */
data class SystemThumbnail(val uri: Uri)
