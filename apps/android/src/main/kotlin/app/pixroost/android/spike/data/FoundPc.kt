package app.pixroost.android.spike.data

/** The PC that broadcast "who is there": its address is where the direct path goes. */
data class FoundPc(val name: String, val host: String, val foundAfterMillis: Long)
