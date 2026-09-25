package app.pixroost.android.spike.report

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log

/** Copies the report and writes it to Logcat for runs on the emulator. */
fun Context.copyReport(report: String) {
    Log.i("S04", report)
    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Pixroost S-04", report))
}
