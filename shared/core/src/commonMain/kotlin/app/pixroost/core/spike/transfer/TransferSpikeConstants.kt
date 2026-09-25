package app.pixroost.core.spike.transfer

/**
 * Spike S-03: the upload protocol shared by the phone and the PC, in the spirit of tus (ADR 0005).
 * The upload id is the file's SHA-256, so a phone can resume after either side restarts.
 */
object TransferSpikeConstants {
    const val INFO_PATH = "/v1/info"
    const val UPLOADS_PATH = "/v1/uploads"

    /** Bytes the PC already has; the phone sends the rest starting from here. */
    const val HEADER_OFFSET = "Upload-Offset"
    const val HEADER_LENGTH = "Upload-Length"

    /** Lowercase hex SHA-256 of the whole file; the PC checks it before the file goes to the archive. */
    const val HEADER_SHA256 = "Upload-Sha256"

    /** The file name, percent-encoded because HTTP headers are ASCII. */
    const val HEADER_NAME = "Upload-Name"

    /** "1" when the PC has the whole file and its SHA-256 matched. */
    const val HEADER_COMPLETE = "Upload-Complete"

    /** Idle connections the PC keeps open to the phone for the reversed path. */
    const val REVERSE_POOL_SIZE = 2

    /** The PC replaces an idle reversed connection after this time, so a dead one is never used. */
    const val REVERSE_IDLE_MILLIS = 15_000

    /** The phone drops a waiting reversed connection older than this, a bit before the PC does. */
    const val REVERSE_MAX_AGE_MILLIS = 12_000L

    const val COPY_BUFFER_SIZE = 256 * 1024
    const val SHA256_HEX_LENGTH = 64
}
