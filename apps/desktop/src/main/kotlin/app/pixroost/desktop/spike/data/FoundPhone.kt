package app.pixroost.desktop.spike.data

/** A phone the PC found on the network, and how. */
data class FoundPhone(
    val name: String,
    val host: String,
    val port: Int,
    val method: DiscoveryMethod,
    val foundAfterMillis: Long,
)
