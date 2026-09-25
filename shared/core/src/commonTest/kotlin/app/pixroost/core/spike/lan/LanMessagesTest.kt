package app.pixroost.core.spike.lan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LanMessagesTest {
    @Test
    fun `reads back the fields it writes`() {
        val message = lanMessage(LanSpikeConstants.PHONE_HERE, "port" to "47201", "name" to "Redmi Note 9")
        assertEquals("PIXROOST-PHONE port=47201 name=Redmi_Note_9", message)
        assertEquals("47201", lanField(message, "port"))
        assertEquals("Redmi_Note_9", lanField(message, "name"))
    }

    @Test
    fun `returns null for a missing field`() {
        assertNull(lanField("PIXROOST-HELLO pc=home", "phone"))
    }
}
