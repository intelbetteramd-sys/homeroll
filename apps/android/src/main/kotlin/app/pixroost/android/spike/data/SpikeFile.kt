package app.pixroost.android.spike.data

/** A file to send: a generated test file (a path) or one picked in the system photo picker (a content URI). */
data class SpikeFile(val name: String, val size: Long, val location: String)
