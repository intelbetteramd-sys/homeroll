package app.pixroost.android.spike.data

import android.annotation.SuppressLint
import android.content.SharedPreferences
import app.pixroost.android.spike.util.sha256Hex
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.X509TrustManager

/**
 * Trusts only the PC certificate with the pinned SHA-256 fingerprint. Until pairing by QR exists, the first
 * certificate seen is pinned (trust on first use); the screen shows it to compare with the PC window.
 * The host name is not checked: the reversed path connects to 127.0.0.1, and the pin is stronger anyway.
 */
@SuppressLint("CustomX509TrustManager")
class PinningTrust(private val preferences: SharedPreferences, private val onPinned: (String) -> Unit) :
    X509TrustManager {
    val sslContext: SSLContext = SSLContext.getInstance("TLS").apply { init(null, arrayOf(this@PinningTrust), null) }

    var pinned: String? = preferences.getString(LanDataConstants.PINNED_FINGERPRINT, null)
        private set

    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        val fingerprint = sha256Hex(chain.first().encoded)
        val pin = pinned ?: fingerprint.also(::pin)
        if (fingerprint != pin) throw CertificateException("сертификат ПК не совпал с запомненным")
    }

    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) =
        throw CertificateException("телефон не принимает клиентские сертификаты")

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()

    /** Runs after [checkServerTrusted], so the pin is set by then. */
    fun verifyHost(@Suppress("UNUSED_PARAMETER") hostname: String, session: SSLSession): Boolean =
        session.peerCertificates.firstOrNull()?.let { sha256Hex(it.encoded) == pinned } ?: false

    fun forget() {
        pinned = null
        preferences.edit().remove(LanDataConstants.PINNED_FINGERPRINT).apply()
    }

    private fun pin(fingerprint: String) {
        pinned = fingerprint
        preferences.edit().putString(LanDataConstants.PINNED_FINGERPRINT, fingerprint).apply()
        onPinned(fingerprint)
    }
}
