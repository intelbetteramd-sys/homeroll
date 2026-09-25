package app.pixroost.core.spike.oauth

object OAuthSpikeConstants {
    const val VERIFIER_BYTES = 32
    const val STATE_BYTES = 16
    const val MILLIS_IN_SECOND = 1000L
    const val ERROR_BODY_CHARS = 300
    const val TENTHS = 10L
    const val BYTES_IN_GIGABYTE = 1024L * 1024 * 1024

    const val PICKER_API = "https://photospicker.googleapis.com/v1/"
    const val PICKER_PAGE_SIZE = 100
    const val DEFAULT_POLL_MILLIS = 3000L
    const val DOWNLOAD_BUFFER = 64 * 1024
    const val BYTES_IN_KILOBYTE = 1024L
    const val MILLIS_IN_MINUTE = 60_000L
    const val MILLIS_IN_DAY = 24 * 60 * 60_000L

    /** The app's own redirect scheme on Android; Google requires it to equal the application ID. */
    const val ANDROID_SCHEME = "app.pixroost.spike.oauth"

    /** For the desktop's browser tab: the Picker page closes itself after "Done". */
    const val PICKER_AUTOCLOSE = "/autoclose"

    /** How long a sign-in in the browser may take before the app gives up waiting for the redirect. */
    const val SIGN_IN_TIMEOUT_MILLIS = 5 * 60_000L
}
