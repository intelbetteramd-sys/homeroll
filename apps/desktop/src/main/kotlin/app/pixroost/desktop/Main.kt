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
import app.pixroost.desktop.spike.ui.LanSpikeController
import app.pixroost.desktop.spike.ui.LanSpikeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Spike S-04: the LAN discovery window instead of the placeholder screen. */
fun main() = application {
    val controller = remember {
        LanSpikeController(CoroutineScope(SupervisorJob() + Dispatchers.Default)).also { it.start() }
    }
    Window(
        onCloseRequest = {
            controller.close()
            exitApplication()
        },
        title = "Pixroost S-04",
        state = rememberWindowState(width = 1100.dp, height = 800.dp),
    ) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            LanSpikeScreen(controller)
        }
    }
}
