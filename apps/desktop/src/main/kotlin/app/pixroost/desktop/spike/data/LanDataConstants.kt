package app.pixroost.desktop.spike.data

object LanDataConstants {
    const val BROADCAST_ALL = "255.255.255.255"
    const val RECEIVE_TIMEOUT_MILLIS = 250
    const val DATAGRAM_BUFFER_SIZE = 512

    // Windows firewall. Commands use no double quotes: Windows drops them when it passes a Java
    // argument to PowerShell, so strings are single-quoted and formatted with -f.
    const val RULE_NAME = "Pixroost S-03"
    const val POWERSHELL_TIMEOUT_SECONDS = 60L
    const val UTF8_OUTPUT = "[Console]::OutputEncoding = [Text.Encoding]::UTF8; "
    const val PROFILES_COMMAND =
        "Get-NetConnectionProfile | ForEach-Object { '{0}: {1}' -f \$_.InterfaceAlias, \$_.NetworkCategory }"

    // Received files and the TLS certificate.
    const val SPIKE_FOLDER = "Pixroost S-03"
    const val INCOMING_FOLDER = ".incoming"
    const val KEYSTORE_FILE = ".server-certificate.p12"
    const val KEY_ALIAS = "pixroost"

    /** Protects only a throwaway spike certificate on this PC; the app keeps its key in the OS keystore. */
    const val KEYSTORE_PASSWORD = "pixroost-spike"
    const val CERTIFICATE_NAME = "CN=Pixroost S-03"
    const val CERTIFICATE_DAYS = 397L
    const val SERIAL_BITS = 63

    // Server
    const val REQUEST_READ_TIMEOUT_SECONDS = 20
    const val PROGRESS_STEP_BYTES = 4L * 1024 * 1024
    const val REVERSE_RETRY_MILLIS = 2000L
    const val REVERSE_POLL_MILLIS = 200L
    const val LOOPBACK = "127.0.0.1"
    const val STOP_TIMEOUT_MILLIS = 1000L
}
