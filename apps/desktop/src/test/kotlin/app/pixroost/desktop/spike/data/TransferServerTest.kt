package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.core.spike.transfer.TransferSpikeConstants
import app.pixroost.desktop.spike.util.sha256Hex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.io.File
import java.io.InputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

/** Runs the spike's HTTPS server on port 47200 and talks to it the way the phone does. */
class TransferServerTest {
    private val root: File = Files.createTempDirectory("s03").toFile()
    private val store = TransferStore(root)
    private val handler = UploadHandler(store, "test-pc") {}
    private val certificate = ServerCertificate(File(root, "cert.p12"))
    private val server = TransferServer(certificate, handler)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @BeforeTest
    fun startServer() = server.start()

    @AfterTest
    fun stopServer() {
        scope.cancel()
        server.stop()
        root.deleteRecursively()
    }

    @Test
    fun resumesAfterBrokenConnectionAndChecksSha256() {
        val content = Random(1).nextBytes(3_000_000)
        val id = sha256Hex(content)
        val created = request(direct(), "POST", uploads(), headersFor(content, id))
        assertEquals("201", created.status)
        assertEquals("0", created.headers[TransferSpikeConstants.HEADER_OFFSET])

        // The connection breaks after a third of the file: the PC keeps what arrived.
        direct().use { socket ->
            socket.outputStream.write(head("PATCH", "${uploads()}/$id", patchHeaders(0, content.size)))
            socket.outputStream.write(content, 0, content.size / 3)
            socket.outputStream.flush()
        }
        Thread.sleep(SETTLE_MILLIS)
        val offset = request(direct(), "HEAD", "${uploads()}/$id")
            .headers.getValue(TransferSpikeConstants.HEADER_OFFSET).toInt()
        assertEquals(content.size / 3, offset)

        val rest = content.copyOfRange(offset, content.size)
        val done = request(direct(), "PATCH", "${uploads()}/$id", patchHeaders(offset, rest.size), rest)
        assertEquals("204", done.status)
        assertEquals("1", done.headers[TransferSpikeConstants.HEADER_COMPLETE])
        assertContentEquals(content, File(root, "photo.jpg").readBytes())
        assertEquals(UploadStatus.Done, handler.uploads.value.getValue(id).status)
    }

    @Test
    fun uploadsThroughReversedConnectionOpenedByThePc() {
        val content = Random(2).nextBytes(1_000_000)
        val id = sha256Hex(content)
        ServerSocket(0).use { phone ->
            ReverseLinks(scope) {}.open("127.0.0.1", phone.localPort)
            val created = request(reversed(phone.accept()), "POST", uploads(), headersFor(content, id))
            assertEquals("201", created.status)
            val done = request(
                reversed(phone.accept()),
                "PATCH",
                "${uploads()}/$id",
                patchHeaders(0, content.size),
                content,
            )
            assertEquals("1", done.headers[TransferSpikeConstants.HEADER_COMPLETE])
        }
        assertContentEquals(content, File(root, "photo.jpg").readBytes())
    }

    private fun uploads() = TransferSpikeConstants.UPLOADS_PATH

    private fun headersFor(content: ByteArray, id: String) = mapOf(
        TransferSpikeConstants.HEADER_SHA256 to id,
        TransferSpikeConstants.HEADER_LENGTH to content.size.toString(),
        TransferSpikeConstants.HEADER_NAME to "photo.jpg",
    )

    private fun patchHeaders(offset: Int, length: Int) = mapOf(
        TransferSpikeConstants.HEADER_OFFSET to offset.toString(),
        "Content-Length" to length.toString(),
    )

    private fun direct(): SSLSocket =
        tls.socketFactory.createSocket("127.0.0.1", LanSpikeConstants.PC_PORT) as SSLSocket

    /** TLS as the phone does it: over a connection the PC opened, with the phone as the TLS client. */
    private fun reversed(accepted: Socket): SSLSocket =
        tls.socketFactory.createSocket(accepted, "127.0.0.1", LanSpikeConstants.PC_PORT, true) as SSLSocket

    private fun head(method: String, path: String, headers: Map<String, String>): ByteArray =
        (listOf("$method $path HTTP/1.1", "Host: pc", "Connection: close") + headers.map { "${it.key}: ${it.value}" })
            .joinToString("\r\n", postfix = "\r\n\r\n").toByteArray()

    private fun request(
        socket: SSLSocket,
        method: String,
        path: String,
        headers: Map<String, String> = emptyMap(),
        body: ByteArray = ByteArray(0),
    ): Response = socket.use {
        val allHeaders = if (method == "POST") headers + ("Content-Length" to "0") else headers
        it.outputStream.write(head(method, path, allHeaders))
        it.outputStream.write(body)
        it.outputStream.flush()
        readResponse(it.inputStream)
    }

    private fun readResponse(input: InputStream): Response {
        val lines = generateSequence { readLine(input) }.takeWhile { it.isNotEmpty() }.toList()
        val headers = lines.drop(1).associate { it.substringBefore(':').trim() to it.substringAfter(':').trim() }
        return Response(lines.first().split(' ')[1], headers)
    }

    private fun readLine(input: InputStream): String = buildString {
        while (true) {
            val next = input.read()
            if (next < 0 || next == '\n'.code) break
            if (next != '\r'.code) append(next.toChar())
        }
    }

    private data class Response(val status: String, val headers: Map<String, String>)

    private companion object {
        const val SETTLE_MILLIS = 500L

        /** Accepts any certificate: the tests check the protocol, the phone checks the fingerprint. */
        val tls: SSLContext = SSLContext.getInstance("TLS").apply {
            val trustAll = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
                override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
            }
            init(null, arrayOf<TrustManager>(trustAll), null)
        }
    }
}
