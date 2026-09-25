package app.pixroost.android.spike.data

object LanDataConstants {
    const val MULTICAST_LOCK_TAG = "pixroost-s03"
    const val DATAGRAM_BUFFER_SIZE = 512

    // Upload client
    const val CONNECT_TIMEOUT_MILLIS = 3000L
    const val SOCKET_TIMEOUT_MILLIS = 15_000L
    const val RETRY_DELAY_MILLIS = 2000L
    const val RETRY_WINDOW_MILLIS = 5 * 60_000L
    const val PARALLEL_SMALL_FILES = 3
    const val LARGE_FILE_BYTES = 50L * 1024 * 1024
    const val PROGRESS_STEP_BYTES = 1024L * 1024

    // Test files: photos and one video, filled with random bytes so every run sends new files.
    const val TEST_FOLDER = "s03-test"
    const val TEST_PHOTO_COUNT = 20
    const val TEST_PHOTO_BYTES = 3L * 1024 * 1024
    const val TEST_VIDEO_BYTES = 500L * 1024 * 1024
    const val TEST_WRITE_BUFFER = 1024 * 1024

    // Reversed path
    const val LOOPBACK = "127.0.0.1"
    const val WAIT_FOR_PC_MILLIS = 3000L
    const val WAIT_POLL_MILLIS = 50L
    const val LOCAL_BACKLOG = 16

    // Trust on first use, until pairing by QR exists.
    const val PREFERENCES = "s03"
    const val PINNED_FINGERPRINT = "pinned-fingerprint"
}
