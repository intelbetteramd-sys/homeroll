package app.pixroost.android.spike.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.TokenSet
import app.pixroost.core.spike.oauth.toJson
import app.pixroost.core.spike.oauth.tokenSetFromJson
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Tokens encrypted with an AES key that lives in Android Keystore and never leaves it (hardware-backed where the
 * phone has it). The ciphertext is kept in the app's private preferences.
 */
class KeystoreTokenStore(context: Context) {
    private val preferences = context.getSharedPreferences(DataConstants.TOKEN_PREFERENCES, Context.MODE_PRIVATE)
    private val keyStore = KeyStore.getInstance(DataConstants.ANDROID_KEYSTORE).apply { load(null) }

    fun load(service: CloudService): TokenSet? = preferences.getString(service.name, null)?.let { stored ->
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(DataConstants.CIPHER)
        val iv = bytes.copyOfRange(0, DataConstants.GCM_IV_BYTES)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(DataConstants.GCM_TAG_BITS, iv))
        tokenSetFromJson(String(cipher.doFinal(bytes, DataConstants.GCM_IV_BYTES, bytes.size - iv.size)))
    }

    fun save(service: CloudService, tokens: TokenSet) {
        val cipher = Cipher.getInstance(DataConstants.CIPHER).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val sealed = cipher.iv + cipher.doFinal(tokens.toJson().toByteArray())
        preferences.edit().putString(service.name, Base64.encodeToString(sealed, Base64.NO_WRAP)).apply()
    }

    /** Signing out forgets the app's own copy of the token; the user's files are not touched. */
    fun forget(service: CloudService) {
        preferences.edit().remove(service.name).apply()
    }

    private fun key(): SecretKey = (keyStore.getKey(DataConstants.KEY_ALIAS, null) as? SecretKey)
        ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, DataConstants.ANDROID_KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(
                    DataConstants.KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(DataConstants.AES_KEY_BITS)
                    .build(),
            )
        }.generateKey()
}
