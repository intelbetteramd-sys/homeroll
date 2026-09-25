package app.pixroost.android.spike.ui.model

import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.Pkce

/** A sign-in waiting for its redirect: the [state] must come back unchanged, [pkce] finishes the exchange. */
data class PendingSignIn(val service: CloudService, val state: String, val pkce: Pkce, val redirectUri: String)
