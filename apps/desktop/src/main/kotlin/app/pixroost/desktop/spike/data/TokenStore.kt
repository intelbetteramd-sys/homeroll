package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.TokenSet
import app.pixroost.core.spike.oauth.toJson
import app.pixroost.core.spike.oauth.tokenSetFromJson
import com.sun.jna.platform.win32.Crypt32Util
import java.io.File

/**
 * Tokens on the PC, one file per service. On Windows the file is encrypted with DPAPI for the current user: only
 * this Windows account can decrypt it, and there is no size limit like the 2.5 KB of Credential Manager.
 * Elsewhere the spike keeps the file as is and says so.
 */
class TokenStore(private val folder: File) {
    val isEncrypted: Boolean = System.getProperty("os.name").orEmpty().startsWith("Windows")

    fun load(service: CloudService): TokenSet? = file(service).takeIf { it.exists() }?.let { file ->
        val bytes = file.readBytes()
        tokenSetFromJson(String(if (isEncrypted) Crypt32Util.cryptUnprotectData(bytes) else bytes))
    }

    fun save(service: CloudService, tokens: TokenSet) {
        folder.mkdirs()
        val bytes = tokens.toJson().toByteArray()
        file(service).writeBytes(if (isEncrypted) Crypt32Util.cryptProtectData(bytes) else bytes)
    }

    /** Signing out forgets the app's own token file; the user's files are not touched. */
    fun forget(service: CloudService) {
        file(service).delete()
    }

    private fun file(service: CloudService) = File(folder, "${service.name.lowercase()}.token")
}
