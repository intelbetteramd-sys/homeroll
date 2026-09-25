package app.pixroost.core.spike.oauth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RedirectCodeTest {
    @Test
    fun returnsTheCodeWhenStateMatches() {
        val reply = mapOf("code" to "abc", "state" to "s1")

        assertEquals("abc", codeFromRedirect("s1", reply::get))
    }

    @Test
    fun rejectsSomeoneElsesRedirect() {
        val reply = mapOf("code" to "abc", "state" to "other")

        assertFailsWith<OAuthException> { codeFromRedirect("s1", reply::get) }
    }

    @Test
    fun reportsTheServiceError() {
        val reply = mapOf("error" to "access_denied", "error_description" to "user said no", "state" to "s1")

        val error = assertFailsWith<OAuthException> { codeFromRedirect("s1", reply::get) }
        assertEquals("access_denied: user said no", error.message)
    }
}
