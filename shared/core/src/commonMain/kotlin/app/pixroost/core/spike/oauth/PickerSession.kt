package app.pixroost.core.spike.oauth

/** A Google Photos Picker session: the user picks in Google's own page at [pickerUri]. */
data class PickerSession(
    val id: String,
    val pickerUri: String,
    val isMediaItemsSet: Boolean,
    val pollIntervalMillis: Long,
)
