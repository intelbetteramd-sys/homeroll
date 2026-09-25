package app.pixroost.core.spike.oauth

/** A photo or video the user picked in Google Photos. [baseUrl] works for about an hour and needs the token. */
data class PickedMedia(val id: String, val fileName: String, val mimeType: String, val baseUrl: String) {
    val isVideo: Boolean get() = mimeType.startsWith("video/")
}
