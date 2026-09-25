package app.pixroost.desktop.spike.data

/** Reversed connections to one phone: waiting for the phone, carrying its uploads, opened in total, failed. */
data class ReverseLinkStats(val idle: Int = 0, val active: Int = 0, val opened: Int = 0, val failed: Int = 0)
