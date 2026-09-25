package app.pixroost.android.spike.data

/** The PC's answer to "I have this file": how many bytes it already has and whether it has all of them. */
data class UploadOffer(val offset: Long, val isComplete: Boolean)
