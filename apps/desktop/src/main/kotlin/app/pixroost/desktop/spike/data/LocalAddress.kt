package app.pixroost.desktop.spike.data

import java.net.InetAddress

/** An IPv4 address of this PC and the broadcast address of its network. */
data class LocalAddress(val interfaceName: String, val address: InetAddress, val broadcast: InetAddress?)
