package app.pixroost.desktop.spike.util

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import java.io.File
import java.security.MessageDigest

/** Lowercase hex SHA-256 of [bytes]. */
fun sha256Hex(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

/** Lowercase hex SHA-256 of a file, read in chunks. Blocking. */
fun sha256Hex(file: File): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(TransferSpikeConstants.COPY_BUFFER_SIZE)
    file.inputStream().use { input ->
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().toHex()
}

/** True for a lowercase hex SHA-256: the upload id, safe to use as a file name. */
fun isSha256Hex(value: String): Boolean =
    value.length == TransferSpikeConstants.SHA256_HEX_LENGTH && value.all { it in '0'..'9' || it in 'a'..'f' }

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
