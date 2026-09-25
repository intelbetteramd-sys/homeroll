package app.pixroost.core

actual fun platformName(): String {
    val name = System.getProperty("os.name").orEmpty()
    // Windows reports its marketing name ("Windows 11"); other systems need the version appended.
    return if (name.startsWith("Windows")) name else "$name ${System.getProperty("os.version").orEmpty()}".trim()
}
