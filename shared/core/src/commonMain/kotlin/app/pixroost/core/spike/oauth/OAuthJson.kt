package app.pixroost.core.spike.oauth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/** Reads JSON answers without generated serializers: the spike needs only a few fields of each. */
object OAuthJson {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): JsonElement = json.parseToJsonElement(text)
}
