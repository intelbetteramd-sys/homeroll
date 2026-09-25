package app.pixroost.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppInfoTest {
    @Test
    fun appNameIsPixroost() {
        assertEquals("Pixroost", AppInfo.NAME)
    }

    @Test
    fun platformNameIsNotBlank() {
        assertTrue(platformName().isNotBlank())
    }
}
