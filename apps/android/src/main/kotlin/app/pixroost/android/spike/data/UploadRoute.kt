package app.pixroost.android.spike.data

/** Which way the phone reaches the PC's server. */
enum class UploadRoute(val label: String) {
    /** The phone connects to the PC: needs the firewall rule. */
    Direct("прямой"),

    /** Through connections the PC opened to the phone: no firewall rule needed. */
    Reversed("развёрнутый"),
}
