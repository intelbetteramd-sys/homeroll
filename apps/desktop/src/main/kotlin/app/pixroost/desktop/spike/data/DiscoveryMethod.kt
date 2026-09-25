package app.pixroost.desktop.spike.data

enum class DiscoveryMethod(val label: String) {
    /** The PC broadcasts "who is there" and the phone answers it directly. */
    Broadcast("рассылка"),

    /** The PC browses for the phone's mDNS service. */
    Mdns("mDNS"),
}
