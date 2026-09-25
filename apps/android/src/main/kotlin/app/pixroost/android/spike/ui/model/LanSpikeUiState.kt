package app.pixroost.android.spike.ui.model

data class LanSpikeUiState(
    val phoneName: String,
    val addresses: List<String> = emptyList(),
    val pcs: List<PcFinding> = emptyList(),
    val serverStatus: String = "запускается…",
    val discoverRequests: List<DiscoverRequests> = emptyList(),
    val pcGreetings: List<String> = emptyList(),
    val log: List<LanEvent> = emptyList(),
)
