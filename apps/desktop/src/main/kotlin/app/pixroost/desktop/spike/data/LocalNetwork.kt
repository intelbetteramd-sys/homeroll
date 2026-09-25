package app.pixroost.desktop.spike.data

import java.net.Inet4Address
import java.net.NetworkInterface

/** Names of adapters that are not the home network: Hyper-V, WSL, VirtualBox, VMware, VPN. */
private val virtualAdapterHints =
    listOf("vethernet", "virtualbox", "vmware", "wsl", "hyper-v", "loopback", "tap", "tun")

/** IPv4 addresses of real, running network adapters of this PC. */
fun localIpv4Addresses(): List<LocalAddress> = NetworkInterface.networkInterfaces().toList()
    .filter { it.isUp && !it.isLoopback && !it.isVirtual }
    .filterNot { adapter ->
        val name = (adapter.displayName + " " + adapter.name).lowercase()
        virtualAdapterHints.any { it in name }
    }
    .flatMap { adapter ->
        adapter.interfaceAddresses
            .filter { it.address is Inet4Address && it.address.isSiteLocalAddress }
            .map { LocalAddress(adapter.displayName, it.address, it.broadcast) }
    }
