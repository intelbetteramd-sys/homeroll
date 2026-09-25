package app.pixroost.core.spike.lan

/** One-line text messages: `PREFIX key=value key=value`. Values must not contain spaces. */
fun lanMessage(prefix: String, vararg fields: Pair<String, String>): String =
    (listOf(prefix) + fields.map { (key, value) -> "$key=${value.replace(' ', '_')}" }).joinToString(" ")

/** `field("PIXROOST-PHONE port=47201 name=Pixel", "port")` → `"47201"`. */
fun lanField(message: String, key: String): String? =
    message.split(' ').drop(1).firstOrNull { it.startsWith("$key=") }?.substringAfter('=')
