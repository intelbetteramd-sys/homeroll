package app.pixroost.core.spike.oauth

import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenJsonTest {
    @Test
    fun keepsTheRefreshTokenWhenTheReplyOmitsIt() {
        val previous = TokenSet("old", refreshToken = "refresh", expiresAtMillis = 1, scope = "files.read")
        val reply = OAuthJson.parse("""{"access_token":"new","expires_in":3600,"token_type":"bearer"}""").jsonObject

        val tokens = tokenSetFromResponse(reply, nowMillis = 1_000, previous = previous)

        assertEquals(TokenSet("new", "refresh", 3_601_000, "files.read"), tokens)
    }

    @Test
    fun survivesTheStore() {
        val tokens = TokenSet("access", refreshToken = null, expiresAtMillis = 42, scope = null)

        assertEquals(tokens, tokenSetFromJson(tokens.toJson()))
    }
}
