package app.pixroost.core.spike.oauth

/** PKCE (RFC 7636): the app keeps [verifier] and sends only [challenge], a SHA-256 of it, with the sign-in. */
data class Pkce(val verifier: String, val challenge: String)
