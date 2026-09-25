package app.pixroost.desktop.spike.data

/** How the phone's upload reached the server. */
enum class UploadPath(val label: String) {
    /** The phone connected to the PC: needs the firewall rule. */
    Direct("прямой"),

    /** The PC connected to the phone and relayed the connection to its own server on 127.0.0.1. */
    Reversed("развёрнутый"),
}
