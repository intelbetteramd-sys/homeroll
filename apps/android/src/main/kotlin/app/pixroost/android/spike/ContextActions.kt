package app.pixroost.android.spike

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log

/** Copies the report and writes it to Logcat for runs on the emulator. */
fun Context.copyReport(report: String) {
    Log.i(SpikeConstants.LOG_TAG, report)
    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Pixroost S-02", report))
}

/** The app's page in system Settings, where access to photos can be changed. */
fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}
