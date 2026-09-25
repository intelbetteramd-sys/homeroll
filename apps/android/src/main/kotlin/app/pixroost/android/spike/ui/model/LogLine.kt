package app.pixroost.android.spike.ui.model

/** A line of the event log: milliseconds since the app started and what happened. */
data class LogLine(val elapsedMillis: Long, val text: String)
