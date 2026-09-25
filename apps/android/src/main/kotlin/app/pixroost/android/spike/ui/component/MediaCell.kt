package app.pixroost.android.spike.ui.component

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import app.pixroost.android.spike.data.MediaItem
import app.pixroost.android.spike.image.SystemThumbnail
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.GridOptions
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest

/** One square thumbnail; videos get a ▶ mark. */
@Composable
fun MediaCell(item: MediaItem, options: GridOptions, imageLoader: ImageLoader, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val request = remember(item.id, options.systemThumbnails, options.memoryCache) {
        val useSystem = options.systemThumbnails && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        ImageRequest.Builder(context)
            .data(if (useSystem) SystemThumbnail(item.uri) else item.uri)
            .memoryCachePolicy(if (options.memoryCache) CachePolicy.ENABLED else CachePolicy.DISABLED)
            .build()
    }
    Box(modifier.aspectRatio(1f).background(MaterialTheme.colorScheme.surfaceVariant)) {
        AsyncImage(
            model = request,
            contentDescription = null,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (item.isVideo) {
            Text(
                "▶",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomStart).padding(UiConstants.BADGE_PADDING),
            )
        }
    }
}
