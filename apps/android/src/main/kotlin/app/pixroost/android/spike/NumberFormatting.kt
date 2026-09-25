package app.pixroost.android.spike

/** 12345 → "12 345" (grouping by the device locale). */
fun Int.formatted(): String = "%,d".format(this)

fun Float.oneDecimal(): String = "%.1f".format(this)
