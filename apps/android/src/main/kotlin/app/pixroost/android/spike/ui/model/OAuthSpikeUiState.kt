package app.pixroost.android.spike.ui.model

import app.pixroost.core.spike.oauth.CloudService

data class OAuthSpikeUiState(
    val services: Map<CloudService, ServiceUiState> = CloudService.entries.associateWith { ServiceUiState(it) },
    val tokenStorage: String = "",
    /** The package and the signing certificate's fingerprints, asked for when registering the app. */
    val signing: List<String> = emptyList(),
    val log: List<LogLine> = emptyList(),
)
