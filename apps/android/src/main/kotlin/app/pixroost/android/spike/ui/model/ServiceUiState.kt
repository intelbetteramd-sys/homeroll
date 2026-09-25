package app.pixroost.android.spike.ui.model

import app.pixroost.core.spike.oauth.CloudService

/** One cloud on the screen: whether the app has a token, what the token allows, what the service answered. */
data class ServiceUiState(
    val service: CloudService,
    val status: String = "не подключено",
    val account: String? = null,
    val tokenInfo: String? = null,
    val isBusy: Boolean = false,
)
