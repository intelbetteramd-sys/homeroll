package app.pixroost.desktop.spike.util

/** "12.3 МБ". */
fun formatMegabytes(bytes: Long): String = "%.1f МБ".format(bytes / FormatConstants.BYTES_IN_MEGABYTE)

/** "23.4 МБ/с", or "—" when no time passed. */
fun formatSpeed(bytes: Long, millis: Long): String = if (millis <= 0) {
    "—"
} else {
    "%.1f МБ/с".format(bytes / FormatConstants.BYTES_IN_MEGABYTE / (millis / FormatConstants.MILLIS_IN_SECOND))
}

/** "ab12 cd34 …": a fingerprint split into groups, easy to compare by eye. */
fun formatFingerprint(hex: String): String = hex.chunked(FormatConstants.FINGERPRINT_GROUP).joinToString(" ")
