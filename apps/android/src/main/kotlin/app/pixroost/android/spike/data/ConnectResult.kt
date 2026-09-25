package app.pixroost.android.spike.data

/** An outgoing TCP connection: how long it took, what the other side answered, or why it failed. */
data class ConnectResult(val connectMillis: Long?, val answerMillis: Long?, val answer: String?, val error: String?) {
    val isSuccess: Boolean get() = answer != null
}
