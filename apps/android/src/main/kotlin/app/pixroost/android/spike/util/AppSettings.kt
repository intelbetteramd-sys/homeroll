package app.pixroost.android.spike.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

/** The app's page in system Settings, where access to photos can be changed. */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
