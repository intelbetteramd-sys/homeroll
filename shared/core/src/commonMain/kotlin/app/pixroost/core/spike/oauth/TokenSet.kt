package app.pixroost.core.spike.oauth

/** Tokens of one service. [expiresAtMillis] is when the access token expires, by the device clock. */
data class TokenSet(val accessToken: String, val refreshToken: String?, val expiresAtMillis: Long?, val scope: String?)
