package app.pixroost.android.spike.ui.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import app.pixroost.android.spike.data.MediaAccess
import app.pixroost.android.spike.data.MediaIndex
import app.pixroost.android.spike.data.ScanResult
import app.pixroost.android.spike.data.mediaAccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Re-reads access and rescans MediaStore on every return to the app: access can change in the system
 * picker or in Settings while the app is in the background.
 */
@Composable
fun MediaScanEffect(
    access: MediaAccess,
    resumeCount: Int,
    onAccess: (MediaAccess) -> Unit,
    onScan: (ScanResult?) -> Unit,
) {
    val context = LocalContext.current
    val currentOnAccess by rememberUpdatedState(onAccess)
    val currentOnScan by rememberUpdatedState(onScan)
    LaunchedEffect(resumeCount) { currentOnAccess(context.mediaAccess()) }
    LaunchedEffect(access, resumeCount) {
        val scan = if (access == MediaAccess.None) null else withContext(Dispatchers.IO) { MediaIndex.scan(context) }
        currentOnScan(scan)
    }
}
