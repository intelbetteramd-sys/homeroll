package app.pixroost.desktop.spike.ui.model

import app.pixroost.desktop.spike.data.FirewallState

data class LanSpikeUiState(
    val pcName: String,
    val addresses: List<String> = emptyList(),
    val announceStatus: String = "запускается…",
    val serverStatus: String = "запускается…",
    val greetings: List<String> = emptyList(),
    val phones: List<PhoneFinding> = emptyList(),
    val firewall: FirewallState? = null,
)
