package app.pixroost.desktop.spike.ui.model

/** A line of the event log: milliseconds since the app started and what happened. */
data class LanEvent(val elapsedMillis: Long, val text: String)
