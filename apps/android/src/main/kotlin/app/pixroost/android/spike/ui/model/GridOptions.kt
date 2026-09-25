package app.pixroost.android.spike.ui.model

import android.os.Build
import app.pixroost.android.spike.ui.UiConstants

/** Grid settings the spike compares. */
data class GridOptions(val systemThumbnails: Boolean, val memoryCache: Boolean, val target: Int) {
    companion object {
        fun initial() = GridOptions(
            systemThumbnails = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q,
            memoryCache = true,
            target = UiConstants.TARGET_AS_IS,
        )
    }
}
