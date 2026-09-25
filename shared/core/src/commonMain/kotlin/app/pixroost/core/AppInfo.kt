package app.pixroost.core

/** Facts about the running app that every module may show or log. */
object AppInfo {
    const val NAME: String = "Pixroost"
}

/** Human-readable name and version of the operating system, e.g. "Android 16" or "Windows 11". */
expect fun platformName(): String
