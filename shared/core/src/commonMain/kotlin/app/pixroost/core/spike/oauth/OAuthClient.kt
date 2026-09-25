package app.pixroost.core.spike.oauth

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.serialization.json.jsonObject

/** The authorization code flow with PKCE, the same on Android and the desktop; only the redirect differs. */
class OAuthClient(private val http: HttpClient, private val nowMillis: () -> Long) {
    fun authorizeUrl(config: OAuthConfig, redirectUri: String, state: String, pkce: Pkce): String =
        URLBuilder(config.authorizeUrl).apply {
            parameters.append("response_type", "code")
            parameters.append("client_id", config.clientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("state", state)
            parameters.append("code_challenge", pkce.challenge)
            parameters.append("code_challenge_method", "S256")
            if (config.scopes.isNotEmpty()) parameters.append("scope", config.scopes.joinToString(" "))
            config.extraAuthorizeParams.forEach { (key, value) -> parameters.append(key, value) }
        }.buildString()

    suspend fun exchangeCode(config: OAuthConfig, code: String, redirectUri: String, pkce: Pkce): TokenSet =
        requestToken(config, previous = null) {
            append("grant_type", "authorization_code")
            append("code", code)
            append("redirect_uri", redirectUri)
            append("code_verifier", pkce.verifier)
        }

    suspend fun refresh(config: OAuthConfig, tokens: TokenSet): TokenSet {
        val refreshToken = tokens.refreshToken ?: throw OAuthException("сервис не выдал refresh token")
        return requestToken(config, previous = tokens) {
            append("grant_type", "refresh_token")
            append("refresh_token", refreshToken)
        }
    }

    private suspend fun requestToken(
        config: OAuthConfig,
        previous: TokenSet?,
        fields: io.ktor.http.ParametersBuilder.() -> Unit,
    ): TokenSet {
        val response = http.submitForm(
            url = config.tokenUrl,
            formParameters = parameters {
                append("client_id", config.clientId)
                config.clientSecret?.let { append("client_secret", it) }
                fields()
            },
        )
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw OAuthException("${response.status.value}: ${body.take(OAuthSpikeConstants.ERROR_BODY_CHARS)}")
        }
        return tokenSetFromResponse(OAuthJson.parse(body).jsonObject, nowMillis(), previous)
    }
}
