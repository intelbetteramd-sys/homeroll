package app.pixroost.android.spike.util

import app.pixroost.core.spike.transfer.TransferSpikeConstants
import java.io.InputStream
import java.security.MessageDigest

/** Lowercase hex SHA-256 of [bytes]. */
fun sha256Hex(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).toHex()

/** Lowercase hex SHA-256 of a stream, read to the end in chunks. Blocking. */
fun sha256Hex(input: InputStream): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(TransferSpikeConstants.COPY_BUFFER_SIZE)
    while (true) {
        val read = input.read(buffer)
        if (read < 0) break
        digest.update(buffer, 0, read)
    }
    return digest.digest().toHex()
}

private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
