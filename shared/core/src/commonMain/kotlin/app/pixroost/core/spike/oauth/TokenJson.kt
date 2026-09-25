package app.pixroost.core.spike.oauth

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/** A token response of the service, standard OAuth 2 fields. [previous] keeps a refresh token the reply omits. */
fun tokenSetFromResponse(json: JsonObject, nowMillis: Long, previous: TokenSet? = null): TokenSet = TokenSet(
    accessToken = json.string("access_token") ?: throw OAuthException("в ответе нет access_token"),
    refreshToken = json.string("refresh_token") ?: previous?.refreshToken,
    expiresAtMillis = json["expires_in"]?.jsonPrimitive?.longOrNull
        ?.let { nowMillis + it * OAuthSpikeConstants.MILLIS_IN_SECOND },
    scope = json.string("scope") ?: previous?.scope,
)

/** For the encrypted token store. */
fun TokenSet.toJson(): String = buildJsonObject {
    put("access_token", accessToken)
    refreshToken?.let { put("refresh_token", it) }
    expiresAtMillis?.let { put("expires_at", it) }
    scope?.let { put("scope", it) }
}.toString()

fun tokenSetFromJson(text: String): TokenSet {
    val json = OAuthJson.parse(text).jsonObject
    return TokenSet(
        accessToken = json.string("access_token") ?: throw OAuthException("нет access_token"),
        refreshToken = json.string("refresh_token"),
        expiresAtMillis = json["expires_at"]?.jsonPrimitive?.longOrNull,
        scope = json.string("scope"),
    )
}

private fun JsonObject.string(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
