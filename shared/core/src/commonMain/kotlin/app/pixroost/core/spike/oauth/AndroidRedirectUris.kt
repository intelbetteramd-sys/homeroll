package app.pixroost.core.spike.oauth

/**
 * Where the browser returns on Android. Yandex registers the `yx<client ID>` scheme for an app with the Android
 * platform; the others use the app's own scheme. Google accepts only the `scheme:/path` form with one slash.
 */
fun androidRedirectUri(service: CloudService): String = when (service) {
    CloudService.Yandex -> "yx${ClientIdConstants.YANDEX}:///auth/finish?platform=android"
    CloudService.Dropbox -> "${OAuthSpikeConstants.ANDROID_SCHEME}://oauth/dropbox"
    CloudService.Microsoft -> "${OAuthSpikeConstants.ANDROID_SCHEME}://oauth/microsoft"
    CloudService.Google -> "${OAuthSpikeConstants.ANDROID_SCHEME}:/oauth/google"
}
