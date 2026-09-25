package app.pixroost.desktop.spike.util

/** The last path segment of a name from the phone, without characters Windows does not allow. */
fun safeFileName(name: String): String = name.substringAfterLast('/').substringAfterLast('\\')
    .filter { it >= ' ' && it !in "<>:\"|?*" }
    .trim('.', ' ')
    .ifEmpty { "file" }
