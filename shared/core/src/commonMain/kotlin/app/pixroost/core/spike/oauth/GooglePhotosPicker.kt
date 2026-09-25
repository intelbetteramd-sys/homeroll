package app.pixroost.core.spike.oauth

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject

/**
 * Google Photos Picker API: since March 2025 the only way for an app to read photos the user chooses in Google
 * Photos. Create a session, open its page, poll until the user is done, list the picked items, download them.
 */
class GooglePhotosPicker(private val http: HttpClient) {
    suspend fun createSession(token: String): PickerSession =
        session(json(http.post(OAuthSpikeConstants.PICKER_API + "sessions") { bearer(token) }))

    suspend fun getSession(token: String, id: String): PickerSession =
        session(json(http.get(OAuthSpikeConstants.PICKER_API + "sessions/" + id) { bearer(token) }))

    suspend fun listPicked(token: String, sessionId: String): List<PickedMedia> {
        val items = mutableListOf<PickedMedia>()
        var pageToken: String? = null
        do {
            val page = json(
                http.get(OAuthSpikeConstants.PICKER_API + "mediaItems") {
                    bearer(token)
                    parameter("sessionId", sessionId)
                    parameter("pageSize", OAuthSpikeConstants.PICKER_PAGE_SIZE)
                    pageToken?.let { parameter("pageToken", it) }
                },
            )
            (page["mediaItems"] as? JsonArray).orEmpty().mapNotNullTo(items) { mediaItem(it as? JsonObject) }
            pageToken = page.text("nextPageToken")
        } while (pageToken != null)
        return items
    }

    /** Downloads the original bytes and returns how many arrived; the spike only counts them. */
    suspend fun downloadSize(token: String, media: PickedMedia): Long {
        val suffix = if (media.isVideo) "=dv" else "=d"
        val response = http.get(media.baseUrl + suffix) { bearer(token) }
        if (!response.status.isSuccess()) throw OAuthException("скачивание: ${response.status.value}")
        val channel = response.bodyAsChannel()
        val buffer = ByteArray(OAuthSpikeConstants.DOWNLOAD_BUFFER)
        var total = 0L
        while (true) {
            val read = channel.readAvailable(buffer, 0, buffer.size)
            if (read < 0) break
            total += read
        }
        return total
    }

    suspend fun deleteSession(token: String, id: String) {
        http.delete(OAuthSpikeConstants.PICKER_API + "sessions/" + id) { bearer(token) }
    }

    private fun session(json: JsonObject): PickerSession {
        val interval = ((json["pollingConfig"] as? JsonObject)?.text("pollInterval"))
            ?.removeSuffix("s")?.toDoubleOrNull()
        return PickerSession(
            id = json.text("id") ?: throw OAuthException("нет id сессии"),
            pickerUri = json.text("pickerUri") ?: throw OAuthException("нет pickerUri"),
            isMediaItemsSet = (json["mediaItemsSet"] as? JsonPrimitive)?.booleanOrNull == true,
            pollIntervalMillis = interval?.let { (it * OAuthSpikeConstants.MILLIS_IN_SECOND).toLong() }
                ?: OAuthSpikeConstants.DEFAULT_POLL_MILLIS,
        )
    }

    private fun mediaItem(json: JsonObject?): PickedMedia? {
        val file = json?.get("mediaFile") as? JsonObject ?: return null
        val id = json.text("id")
        val baseUrl = file.text("baseUrl")
        return if (id != null && baseUrl != null) {
            PickedMedia(
                id,
                fileName = file.text("filename") ?: "?",
                mimeType = file.text("mimeType").orEmpty(),
                baseUrl,
            )
        } else {
            null
        }
    }

    private suspend fun json(response: HttpResponse): JsonObject {
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw OAuthException("${response.status.value}: ${body.take(OAuthSpikeConstants.ERROR_BODY_CHARS)}")
        }
        return OAuthJson.parse(body).jsonObject
    }

    private fun HttpRequestBuilder.bearer(token: String) = header("Authorization", "Bearer $token")

    private fun JsonObject.text(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull
}
