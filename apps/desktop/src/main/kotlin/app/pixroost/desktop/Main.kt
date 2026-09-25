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
import app.pixroost.desktop.spike.ui.OAuthSpikeController
import app.pixroost.desktop.spike.ui.OAuthSpikeScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Spike S-05: the cloud sign-in window instead of the placeholder screen. */
fun main() = application {
    val controller = remember {
        OAuthSpikeController(CoroutineScope(SupervisorJob() + Dispatchers.Default)).also { it.start() }
    }
    Window(
        onCloseRequest = {
            controller.close()
            exitApplication()
        },
        title = "Pixroost S-05",
        state = rememberWindowState(width = 900.dp, height = 900.dp),
    ) {
        MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
            OAuthSpikeScreen(controller)
        }
    }
}
