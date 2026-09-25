package app.pixroost.android.spike.ui.model

/**
 * A page for the screen to open. Sign-in goes to Custom Tabs; the Google Photos picker goes to a plain VIEW intent,
 * which the Google Photos app takes over when it is installed.
 */
data class BrowserRequest(val url: String, val inCustomTab: Boolean)
