package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.OAuthConfig
import app.pixroost.core.spike.oauth.oauthConfig
import java.io.File

/**
 * The shared config plus what only the PC needs: Google's desktop client does not exchange a code without its
 * client secret, even with PKCE. Google does not treat that secret as confidential, but the project keeps secrets
 * out of the repository, so the spike reads it from [spikeFolder].
 */
fun desktopOAuthConfig(service: CloudService, spikeFolder: File): OAuthConfig? =
    oauthConfig(service, isDesktop = true)?.let { config ->
        if (service == CloudService.Google) {
            val secret = File(spikeFolder, DataConstants.GOOGLE_SECRET_FILE).takeIf { it.exists() }?.readText()
            config.copy(clientSecret = secret?.trim())
        } else {
            config
        }
    }
