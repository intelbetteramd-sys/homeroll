package app.pixroost.desktop.spike.data

import app.pixroost.desktop.spike.util.safeFileName
import app.pixroost.desktop.spike.util.sha256Hex
import java.io.File
import java.nio.file.Files
import java.util.concurrent.ConcurrentHashMap

/**
 * Part files of unfinished uploads and the folder with finished ones. Nothing is deleted: a file whose
 * SHA-256 does not match is renamed to `.bad` and kept for inspection. Blocking: call on Dispatchers.IO.
 */
class TransferStore(val root: File) {
    private val incoming = File(root, LanDataConstants.INCOMING_FOLDER).apply { mkdirs() }
    private val archived = ConcurrentHashMap<String, File>()

    fun archivedFile(id: String): File? = archived[id]

    fun partFile(id: String): File = File(incoming, "$id.part")

    /** Bytes already received for this upload: 0 when there is no part file. */
    fun offset(id: String): Long = partFile(id).length()

    /** Checks the SHA-256 of the whole part file and moves it to the folder under a free name. */
    fun complete(id: String, name: String): File? {
        val part = partFile(id)
        if (sha256Hex(part) != id) {
            part.renameTo(File(incoming, "$id.bad-${System.currentTimeMillis()}"))
            return null
        }
        val target = freeFile(safeFileName(name))
        Files.move(part.toPath(), target.toPath())
        archived[id] = target
        return target
    }

    private fun freeFile(name: String): File {
        val base = name.substringBeforeLast('.')
        val extension = name.substringAfterLast('.', "").let { if (it.isEmpty()) "" else ".$it" }
        return generateSequence(2) { it + 1 }
            .map { File(root, "$base ($it)$extension") }
            .let { sequenceOf(File(root, name)) + it }
            .first { !it.exists() }
    }
}
