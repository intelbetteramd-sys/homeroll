package app.pixroost.desktop.spike.data

/**
 * One file the phone sends, as the PC sees it. An attempt is one PATCH request: the first one and every
 * resume after a break.
 */
data class ReceivedUpload(
    val id: String,
    val name: String,
    val length: Long,
    val received: Long,
    val path: UploadPath,
    val status: UploadStatus,
    val attempts: Int = 0,
    val attemptStartOffset: Long = 0,
    val attemptStartedAt: Long = 0,
    val firstStartedAt: Long = 0,
    val finishedAt: Long? = null,
    val verifyMillis: Long? = null,
    val savedAs: String? = null,
)
