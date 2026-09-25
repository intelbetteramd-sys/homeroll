package app.pixroost.android.spike.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * The package name and the SHA-1 and SHA-256 of the certificate this APK is signed with: Google's Android client
 * asks for SHA-1, Yandex's Android platform for SHA-256. Fingerprints are public: every copy of the APK carries them.
 */
fun Context.signingFingerprints(): List<String> {
    val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo?.apkContentsSigners
    } else {
        @Suppress("DEPRECATION")
        packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
    }.orEmpty()
    return listOf("Пакет: $packageName") + signatures.flatMap { signature ->
        listOf("SHA-1", "SHA-256").map { algorithm ->
            val digest = MessageDigest.getInstance(algorithm).digest(signature.toByteArray())
            "$algorithm: ${digest.joinToString(":") { "%02X".format(it) }}"
        }
    }
}
