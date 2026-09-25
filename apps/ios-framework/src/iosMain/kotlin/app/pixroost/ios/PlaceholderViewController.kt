package app.pixroost.ios

import androidx.compose.ui.window.ComposeUIViewController
import app.pixroost.designsystem.PixroostPlaceholder
import platform.UIKit.UIViewController

/**
 * The shared Compose start screen as a UIKit view controller.
 * Swift calls it as `PlaceholderViewControllerKt.PlaceholderViewController()`.
 */
@Suppress("FunctionName")
fun PlaceholderViewController(): UIViewController = ComposeUIViewController { PixroostPlaceholder() }
