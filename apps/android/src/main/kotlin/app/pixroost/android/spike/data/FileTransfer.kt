package app.pixroost.android.spike.data

/**
 * One file on its way to the PC. [sent] is what the PC has: the offset it reported plus what went out since.
 * An attempt is one request with the rest of the file; [resumedFrom] lists offsets of the resumed ones.
 */
data class FileTransfer(
    val file: SpikeFile,
    val status: FileStatus = FileStatus.Queued,
    val sent: Long = 0,
    val hashMillis: Long? = null,
    val attempts: Int = 0,
    val resumedFrom: List<Long> = emptyList(),
    val route: UploadRoute? = null,
    val firstAttemptAt: Long? = null,
    val attemptStartOffset: Long = 0,
    val attemptStartedAt: Long = 0,
    val finishedAt: Long? = null,
    val error: String? = null,
)
