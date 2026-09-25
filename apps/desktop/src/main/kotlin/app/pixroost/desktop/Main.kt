package app.pixroost.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.pixroost.core.AppInfo
import app.pixroost.designsystem.PixroostPlaceholder

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = AppInfo.NAME) {
        PixroostPlaceholder()
    }
}
