package app.pixroost.android.spike.data

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Protocol
import java.io.Closeable
import java.io.IOException
import java.net.URLEncoder

/** The upload protocol on the phone: Ktor client on OkHttp, trusting only the pinned PC certificate. */
class UploadClient(private val trust: PinningTrust, private val source: FileSource) : Closeable {
    private val client = HttpClient(OkHttp) {
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = LanDataConstants.CONNECT_TIMEOUT_MILLIS
            socketTimeoutMillis = LanDataConstants.SOCKET_TIMEOUT_MILLIS
        }
        engine {
            config {
                sslSocketFactory(trust.sslContext.socketFactory, trust)
                hostnameVerifier(trust::verifyHost)
                protocols(listOf(Protocol.HTTP_1_1))
            }
        }
    }

    /** Creates the upload or finds the one started before: POST answers with the offset every time. */
    suspend fun offer(baseUrl: String, file: SpikeFile, sha256: String): UploadOffer {
        val response = client.post(baseUrl + TransferSpikeConstants.UPLOADS_PATH) {
            header(TransferSpikeConstants.HEADER_SHA256, sha256)
            header(TransferSpikeConstants.HEADER_LENGTH, file.size)
            header(TransferSpikeConstants.HEADER_NAME, URLEncoder.encode(file.name, Charsets.UTF_8.name()))
        }
        if (!response.status.isSuccess()) throw IOException("ПК ответил ${response.status.value} на создание")
        return response.toOffer()
    }

    /** Sends the file from [offset] to the end in one request; [onBytes] counts what went out. */
    suspend fun send(
        baseUrl: String,
        file: SpikeFile,
        sha256: String,
        offset: Long,
        onBytes: (Int) -> Unit,
    ): UploadOffer {
        val response = client.patch("$baseUrl${TransferSpikeConstants.UPLOADS_PATH}/$sha256") {
            header(TransferSpikeConstants.HEADER_OFFSET, offset)
            setBody(FileBody(file, offset, onBytes))
        }
        return when {
            response.status.isSuccess() || response.status == HttpStatusCode.Conflict -> response.toOffer()
            response.status == HttpStatusCode.UnprocessableEntity -> error("ПК: SHA-256 не совпал")
            else -> throw IOException("ПК ответил ${response.status.value} на отправку")
        }
    }

    override fun close() = client.close()

    private fun HttpResponse.toOffer() = UploadOffer(
        offset = headers[TransferSpikeConstants.HEADER_OFFSET]?.toLongOrNull() ?: 0,
        isComplete = headers[TransferSpikeConstants.HEADER_COMPLETE] == "1",
    )

    private inner class FileBody(
        private val file: SpikeFile,
        private val offset: Long,
        private val onBytes: (Int) -> Unit,
    ) : OutgoingContent.WriteChannelContent() {
        override val contentLength: Long = file.size - offset
        override val contentType: ContentType = ContentType.Application.OctetStream

        override suspend fun writeTo(channel: ByteWriteChannel) {
            val buffer = ByteArray(TransferSpikeConstants.COPY_BUFFER_SIZE)
            withContext(Dispatchers.IO) { source.open(file, offset) }.use { input ->
                while (true) {
                    val read = withContext(Dispatchers.IO) { input.read(buffer) }
                    if (read < 0) break
                    channel.writeFully(buffer, 0, read)
                    onBytes(read)
                }
            }
        }
    }
}
