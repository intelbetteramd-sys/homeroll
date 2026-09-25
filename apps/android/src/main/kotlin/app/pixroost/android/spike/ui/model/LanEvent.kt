package app.pixroost.android.spike.ui.model

/** A line of the event log: milliseconds since the screen started and what happened. */
data class LanEvent(val elapsedMillis: Long, val text: String)
