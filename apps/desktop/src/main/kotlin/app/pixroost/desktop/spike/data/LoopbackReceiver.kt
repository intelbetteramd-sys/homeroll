package app.pixroost.desktop.spike.data

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import java.io.Closeable
import java.io.IOException
import java.net.Inet6Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLDecoder

/**
 * Receives the OAuth redirect on the loopback address (RFC 8252). [port] 0 takes a free port; Dropbox needs a fixed
 * one because it matches the port too. [host] is what the redirect URI says: Microsoft registers `localhost`,
 * which a browser may resolve to IPv6, so the receiver listens on both 127.0.0.1 and ::1. Loopback servers are not
 * reachable from the network, and Windows shows no firewall prompt for them.
 */
class LoopbackReceiver(host: String, port: Int = 0) : Closeable {
    private val result = CompletableDeferred<Map<String, String>>()
    private val servers = mutableListOf<HttpServer>()

    val redirectUri: String

    init {
        val ipv4 = HttpServer.create(InetSocketAddress(InetAddress.getByName("127.0.0.1"), port), 0)
        servers += ipv4
        val boundPort = ipv4.address.port
        try {
            servers += HttpServer.create(InetSocketAddress(Inet6Address.getByName("::1"), boundPort), 0)
        } catch (_: IOException) {
            // No IPv6 loopback on this PC: the redirect goes to 127.0.0.1.
        }
        servers.forEach { server ->
            server.createContext(DataConstants.CALLBACK_PATH, ::answer)
            server.start()
        }
        redirectUri = "http://$host:$boundPort${DataConstants.CALLBACK_PATH}"
    }

    /** The redirect's query parameters: `code` and `state`, or `error`. */
    suspend fun await(timeoutMillis: Long): Map<String, String> = withTimeout(timeoutMillis) { result.await() }

    override fun close() = servers.forEach { it.stop(0) }

    private fun answer(exchange: HttpExchange) {
        val query = exchange.requestURI.rawQuery.orEmpty()
        val page = DataConstants.DONE_PAGE.toByteArray()
        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.sendResponseHeaders(DataConstants.HTTP_OK, page.size.toLong())
        exchange.responseBody.use { it.write(page) }
        result.complete(parseQuery(query))
    }

    private fun parseQuery(query: String): Map<String, String> = query.split('&')
        .filter { it.contains('=') }
        .associate { pair ->
            val (key, value) = pair.split('=', limit = 2)
            URLDecoder.decode(key, Charsets.UTF_8) to URLDecoder.decode(value, Charsets.UTF_8)
        }
}
