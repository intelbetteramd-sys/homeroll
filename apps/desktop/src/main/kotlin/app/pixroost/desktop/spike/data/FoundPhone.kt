package app.pixroost.desktop.spike.data

/** A phone that answered the PC's broadcast: it waits for reversed connections on [port]. */
data class FoundPhone(val name: String, val host: String, val port: Int, val foundAfterMillis: Long)
