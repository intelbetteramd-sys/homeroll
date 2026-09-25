package app.pixroost.core.spike.oauth

/**
 * How to sign in to one service as a public client: no client secret, PKCE instead. [extraAuthorizeParams] are
 * service specifics, for example Dropbox's `token_access_type=offline` for a refresh token.
 */
data class OAuthConfig(
    val service: CloudService,
    val clientId: String,
    val authorizeUrl: String,
    val tokenUrl: String,
    val scopes: List<String>,
    val extraAuthorizeParams: Map<String, String> = emptyMap(),
    /** Only Google's desktop client type has one, and Google does not treat it as confidential. */
    val clientSecret: String? = null,
)
