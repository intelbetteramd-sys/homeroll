package app.pixroost.android.spike.data

import java.io.File
import kotlin.random.Random

/**
 * Photo-sized and video-sized files of random bytes in the app's own folder. Random content makes every set new
 * for the PC, so a repeated run measures a transfer instead of "already in the archive". Files with the same
 * names are overwritten, nothing is deleted. Blocking.
 */
fun createTestFiles(folder: File): List<SpikeFile> {
    folder.mkdirs()
    val photos = (1..LanDataConstants.TEST_PHOTO_COUNT).map { index ->
        writeRandom(File(folder, "test-photo-%02d.jpg".format(index)), LanDataConstants.TEST_PHOTO_BYTES)
    }
    return photos + writeRandom(File(folder, "test-video.mp4"), LanDataConstants.TEST_VIDEO_BYTES)
}

private fun writeRandom(file: File, size: Long): SpikeFile {
    val buffer = ByteArray(LanDataConstants.TEST_WRITE_BUFFER)
    file.outputStream().use { output ->
        var left = size
        while (left > 0) {
            Random.nextBytes(buffer)
            val count = minOf(left, buffer.size.toLong()).toInt()
            output.write(buffer, 0, count)
            left -= count
        }
    }
    return SpikeFile(file.name, size, file.path)
}
