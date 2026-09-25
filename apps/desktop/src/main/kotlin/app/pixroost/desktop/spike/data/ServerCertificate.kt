package app.pixroost.desktop.spike.data

import app.pixroost.desktop.spike.util.sha256Hex
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import java.io.File
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

/**
 * The PC's self-signed TLS certificate on an ECDSA P-256 key, like the one the app will make from the device key.
 * It is kept in a PKCS #12 file, so the fingerprint the phone pinned survives restarts. Blocking.
 */
class ServerCertificate(file: File) {
    val keyStore: KeyStore = if (file.exists()) load(file) else create().also { save(it, file) }

    /** Lowercase hex SHA-256 of the certificate: what the phone pins. */
    val fingerprint: String = sha256Hex(keyStore.getCertificate(LanDataConstants.KEY_ALIAS).encoded)

    private fun load(file: File): KeyStore = KeyStore.getInstance("PKCS12").apply {
        file.inputStream().use { load(it, LanDataConstants.KEYSTORE_PASSWORD.toCharArray()) }
    }

    private fun save(keyStore: KeyStore, file: File) {
        file.parentFile.mkdirs()
        file.outputStream().use { keyStore.store(it, LanDataConstants.KEYSTORE_PASSWORD.toCharArray()) }
    }

    private fun create(): KeyStore {
        val keyPair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(ECGenParameterSpec("secp256r1")) }
            .generateKeyPair()
        val name = X500Name(LanDataConstants.CERTIFICATE_NAME)
        val now = Instant.now()
        val holder = JcaX509v3CertificateBuilder(
            name,
            BigInteger(LanDataConstants.SERIAL_BITS, SecureRandom()).add(BigInteger.ONE),
            Date.from(now.minus(1, ChronoUnit.DAYS)),
            Date.from(now.plus(LanDataConstants.CERTIFICATE_DAYS, ChronoUnit.DAYS)),
            name,
            keyPair.public,
        ).build(JcaContentSignerBuilder("SHA256withECDSA").build(keyPair.private))
        val certificate = JcaX509CertificateConverter().getCertificate(holder)
        return KeyStore.getInstance("PKCS12").apply {
            load(null, null)
            setKeyEntry(
                LanDataConstants.KEY_ALIAS,
                keyPair.private,
                LanDataConstants.KEYSTORE_PASSWORD.toCharArray(),
                arrayOf(certificate),
            )
        }
    }
}
