package app.pixroost.android.spike.data

import java.net.Inet4Address
import java.net.NetworkInterface

/** "wlan0: 192.168.1.5" for every running adapter with a private IPv4 address. */
fun localIpv4Addresses(): List<String> = NetworkInterface.getNetworkInterfaces().toList()
    .filter { it.isUp && !it.isLoopback }
    .flatMap { adapter ->
        adapter.interfaceAddresses
            .filter { it.address is Inet4Address && it.address.isSiteLocalAddress }
            .map { "${adapter.name}: ${it.address.hostAddress}" }
    }
