package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants

object LanDataConstants {
    const val PHONE_SERVICE_TYPE_LOCAL = "${LanSpikeConstants.PHONE_SERVICE_TYPE}.local."
    const val BROADCAST_ALL = "255.255.255.255"
    const val RECEIVE_TIMEOUT_MILLIS = 250
    const val DATAGRAM_BUFFER_SIZE = 512

    // Windows firewall
    const val RULE_NAME = "Pixroost S-04"
    const val POWERSHELL_TIMEOUT_SECONDS = 60L
    const val PROFILES_COMMAND =
        "Get-NetConnectionProfile | ForEach-Object { \"\$(\$_.InterfaceAlias): \$(\$_.NetworkCategory)\" }"
}
