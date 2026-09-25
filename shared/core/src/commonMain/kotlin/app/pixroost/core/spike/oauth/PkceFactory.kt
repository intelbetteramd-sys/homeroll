package app.pixroost.core.spike.oauth

import kotlin.io.encoding.Base64

/**
 * Makes PKCE values and the `state` that ties a redirect to its sign-in. The platform passes its secure random
 * generator and SHA-256, so this code needs no platform crypto of its own.
 */
class PkceFactory(private val randomBytes: (Int) -> ByteArray, private val sha256: (ByteArray) -> ByteArray) {
    private val base64Url = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)

    fun newPkce(): Pkce {
        val verifier = base64Url.encode(randomBytes(OAuthSpikeConstants.VERIFIER_BYTES))
        val challenge = base64Url.encode(sha256(verifier.encodeToByteArray()))
        return Pkce(verifier, challenge)
    }

    fun newState(): String = base64Url.encode(randomBytes(OAuthSpikeConstants.STATE_BYTES))
}
