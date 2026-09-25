package app.pixroost.core.spike.oauth

/**
 * Endpoints and read-only scopes of each service, or `null` while the service has no client ID. Only Google has
 * separate clients for Android and the desktop; the rest use one registration for both.
 */
fun oauthConfig(service: CloudService, isDesktop: Boolean): OAuthConfig? = when (service) {
    CloudService.Yandex -> OAuthConfig(
        service = service,
        clientId = ClientIdConstants.YANDEX,
        authorizeUrl = "https://oauth.yandex.ru/authorize",
        tokenUrl = "https://oauth.yandex.ru/token",
        scopes = listOf("cloud_api:disk.read", "cloud_api:disk.info"),
    )

    CloudService.Dropbox -> OAuthConfig(
        service = service,
        clientId = ClientIdConstants.DROPBOX,
        authorizeUrl = "https://www.dropbox.com/oauth2/authorize",
        tokenUrl = "https://api.dropboxapi.com/oauth2/token",
        scopes = listOf("account_info.read", "files.metadata.read", "files.content.read"),
        extraAuthorizeParams = mapOf("token_access_type" to "offline"),
    )

    CloudService.Microsoft -> OAuthConfig(
        service = service,
        clientId = ClientIdConstants.MICROSOFT,
        authorizeUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/authorize",
        tokenUrl = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token",
        scopes = listOf("offline_access", "User.Read", "Files.Read"),
    )

    CloudService.Google -> OAuthConfig(
        service = service,
        clientId = if (isDesktop) ClientIdConstants.GOOGLE_DESKTOP else ClientIdConstants.GOOGLE_ANDROID,
        authorizeUrl = "https://accounts.google.com/o/oauth2/v2/auth",
        tokenUrl = "https://oauth2.googleapis.com/token",
        scopes = listOf("https://www.googleapis.com/auth/photospicker.mediaitems.readonly"),
        extraAuthorizeParams = mapOf("access_type" to "offline"),
    )
}.takeIf { it.clientId.isNotEmpty() }
