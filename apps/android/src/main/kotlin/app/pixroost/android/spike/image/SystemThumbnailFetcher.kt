package app.pixroost.android.spike.image

import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import coil3.ImageLoader
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import coil3.request.Options
import coil3.size.pxOrElse

/** Loads the thumbnail MediaStore keeps on disk (Android 10+) instead of decoding the original file. */
@RequiresApi(Build.VERSION_CODES.Q)
class SystemThumbnailFetcher(private val data: SystemThumbnail, private val options: Options) : Fetcher {
    override suspend fun fetch(): FetchResult {
        val width = options.size.width.pxOrElse { ImageConstants.DEFAULT_THUMBNAIL_PX }
        val height = options.size.height.pxOrElse { ImageConstants.DEFAULT_THUMBNAIL_PX }
        val bitmap = options.context.contentResolver.loadThumbnail(data.uri, Size(width, height), null)
        return ImageFetchResult(image = bitmap.asImage(), isSampled = true, dataSource = DataSource.DISK)
    }

    class Factory : Fetcher.Factory<SystemThumbnail> {
        override fun create(data: SystemThumbnail, options: Options, imageLoader: ImageLoader): Fetcher? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) SystemThumbnailFetcher(data, options) else null
    }
}
