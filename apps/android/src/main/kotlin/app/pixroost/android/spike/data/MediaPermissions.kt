package app.pixroost.android.spike.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/** Permissions to request for photos and videos on this Android version. */
fun mediaPermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED,
    )

    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VIDEO,
    )

    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

/** Full access, only the photos the user picked (Android 14+), or none. */
fun Context.mediaAccess(): MediaAccess {
    fun granted(permission: String) = checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && granted(Manifest.permission.READ_MEDIA_IMAGES) ->
            MediaAccess.Full

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
            granted(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) -> MediaAccess.Partial

        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU && granted(Manifest.permission.READ_EXTERNAL_STORAGE) ->
            MediaAccess.Full

        else -> MediaAccess.None
    }
}
