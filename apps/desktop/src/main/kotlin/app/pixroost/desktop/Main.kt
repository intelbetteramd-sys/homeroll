package app.pixroost.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import app.pixroost.desktop.spike.ui.TransferSpikeController
import app.pixroost.desktop.spike.ui.TransferSpikeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Spike S-03: the transfer window instead of the placeholder screen. */
fun main() = application {
    val controller = remember {
        TransferSpikeController(CoroutineScope(SupervisorJob() + Dispatchers.Default)).also { it.start() }
    }
    Window(
        onCloseRequest = {
            controller.close()
            exitApplication()
        },
        title = "Pixroost S-03",
        state = rememberWindowState(width = 1100.dp, height = 800.dp),
    ) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            TransferSpikeScreen(controller)
        }
    }
}
