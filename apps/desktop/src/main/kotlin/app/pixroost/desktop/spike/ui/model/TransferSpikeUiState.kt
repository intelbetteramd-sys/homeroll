package app.pixroost.desktop.spike.ui.model

import app.pixroost.desktop.spike.data.FirewallState
import app.pixroost.desktop.spike.data.FoundPhone

data class TransferSpikeUiState(
    val pcName: String,
    val folder: String,
    val addresses: List<String> = emptyList(),
    val serverStatus: String = "запускается…",
    val fingerprint: String = "",
    val announceStatus: String = "…",
    val firewall: FirewallState? = null,
    val phones: List<FoundPhone> = emptyList(),
)
