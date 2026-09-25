package app.pixroost.core.spike.oauth

import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals

class PkceFactoryTest {
    @Test
    fun matchesTheExampleOfRfc7636() {
        // RFC 7636, appendix B: these 32 bytes give the verifier and the challenge below.
        val bytes = intArrayOf(
            116, 24, 223, 180, 151, 153, 224, 37, 79, 250, 96, 125, 216, 173, 187, 186,
            22, 212, 37, 77, 105, 214, 191, 240, 91, 88, 5, 88, 83, 132, 141, 121,
        ).map { it.toByte() }.toByteArray()
        val factory = PkceFactory(
            randomBytes = { bytes.copyOf(it) },
            sha256 = { MessageDigest.getInstance("SHA-256").digest(it) },
        )

        val pkce = factory.newPkce()

        assertEquals("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk", pkce.verifier)
        assertEquals("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM", pkce.challenge)
    }
}
