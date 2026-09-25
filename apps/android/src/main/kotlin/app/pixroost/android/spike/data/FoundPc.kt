package app.pixroost.android.spike.data

/** A PC the phone found through mDNS: when it appeared and when its address was known. */
data class FoundPc(
    val name: String,
    val host: String,
    val port: Int,
    val foundAfterMillis: Long,
    val resolvedAfterMillis: Long,
)
