package app.pixroost.core.spike.oauth

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull

/**
 * One read-only request per service that proves the token works: the used space. The account name is read but not
 * shown, so the report can go into a public issue as is.
 */
class CloudProbe(private val http: HttpClient) {
    suspend fun probe(service: CloudService, accessToken: String): String = when (service) {
        CloudService.Yandex -> yandex(accessToken)
        CloudService.Dropbox -> dropbox(accessToken)
        CloudService.Microsoft -> microsoft(accessToken)
        CloudService.Google -> "вход выполнен; фото — через выбор в Google Фото"
    }

    private suspend fun yandex(token: String): String {
        val disk = json(http.get("https://cloud-api.yandex.net/v1/disk/") { header("Authorization", "OAuth $token") })
        val user = (disk["user"] as? JsonObject)?.text("display_name")
        return "${account(
            user,
        )}, занято ${gigabytes(disk.number("used_space"))} из ${gigabytes(disk.number("total_space"))}"
    }

    private suspend fun dropbox(token: String): String {
        val account = json(http.post("https://api.dropboxapi.com/2/users/get_current_account") { bearer(token) })
        val space = json(http.post("https://api.dropboxapi.com/2/users/get_space_usage") { bearer(token) })
        val name = (account["name"] as? JsonObject)?.text("display_name")
        val allocated = (space["allocation"] as? JsonObject)?.number("allocated")
        return "${account(name)}, занято ${gigabytes(space.number("used"))} из ${gigabytes(allocated)}"
    }

    private suspend fun microsoft(token: String): String {
        val drive = json(http.get("https://graph.microsoft.com/v1.0/me/drive") { bearer(token) })
        val owner = ((drive["owner"] as? JsonObject)?.get("user") as? JsonObject)?.text("displayName")
        val quota = drive["quota"] as? JsonObject
        return "${account(owner)}, занято ${gigabytes(quota?.number("used"))} из ${gigabytes(quota?.number("total"))}"
    }

    private suspend fun json(response: HttpResponse): JsonObject {
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw OAuthException("${response.status.value}: ${body.take(OAuthSpikeConstants.ERROR_BODY_CHARS)}")
        }
        return OAuthJson.parse(body).jsonObject
    }

    private fun io.ktor.client.request.HttpRequestBuilder.bearer(token: String) =
        header("Authorization", "Bearer $token")

    private fun JsonObject.text(key: String): String? = (this[key] as? JsonPrimitive)?.contentOrNull

    private fun JsonObject.number(key: String): Long? = (this[key] as? JsonPrimitive)?.longOrNull

    private fun account(name: String?): String = if (name.isNullOrEmpty()) "имени аккаунта нет" else "аккаунт прочитан"

    private fun gigabytes(bytes: Long?): String = bytes?.let {
        val tenths = it * OAuthSpikeConstants.TENTHS / OAuthSpikeConstants.BYTES_IN_GIGABYTE
        "${tenths / OAuthSpikeConstants.TENTHS},${tenths % OAuthSpikeConstants.TENTHS} ГБ"
    } ?: "?"
}
