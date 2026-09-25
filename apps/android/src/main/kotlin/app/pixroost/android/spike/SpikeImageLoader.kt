package app.pixroost.android.spike

import android.content.Context
import android.os.Build
import coil3.ImageLoader
import coil3.memory.MemoryCache
import coil3.video.VideoFrameDecoder

/** Coil with system thumbnails (Android 10+), video frames and a memory cache of a quarter of the app's RAM. */
fun spikeImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
    .components {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(SystemThumbnailFetcher.Factory())
        add(SystemThumbnailKeyer())
        add(VideoFrameDecoder.Factory())
    }
    .memoryCache { MemoryCache.Builder().maxSizePercent(context, SpikeConstants.MEMORY_CACHE_SHARE).build() }
    .build()
