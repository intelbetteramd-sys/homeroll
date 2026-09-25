package app.pixroost.desktop.spike.ui.model

import app.pixroost.core.spike.oauth.CloudService

data class OAuthSpikeUiState(
    val services: Map<CloudService, ServiceUiState> = CloudService.entries.associateWith { ServiceUiState(it) },
    val tokenStorage: String = "",
    val log: List<LogLine> = emptyList(),
)
