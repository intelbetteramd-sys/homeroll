package app.pixroost.android.spike.ui.model

/** "Who is there" broadcasts from one PC: how many and when the first one arrived. */
data class DiscoverRequests(val host: String, val count: Int, val firstAfterMillis: Long)
