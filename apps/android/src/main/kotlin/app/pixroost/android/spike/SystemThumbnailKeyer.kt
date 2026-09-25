package app.pixroost.android.spike

import coil3.key.Keyer
import coil3.request.Options

/** Memory cache key for [SystemThumbnail], separate from the key of the original image. */
class SystemThumbnailKeyer : Keyer<SystemThumbnail> {
    override fun key(data: SystemThumbnail, options: Options): String = "thumb:${data.uri}"
}
