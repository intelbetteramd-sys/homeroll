package app.pixroost.android.spike.ui.model

/** What the panel's buttons do. */
class PanelActions(
    val requestAccess: () -> Unit,
    val openSettings: () -> Unit,
    val resetStats: () -> Unit,
    val startAutoScroll: () -> Unit,
    val copyReport: () -> Unit,
)
