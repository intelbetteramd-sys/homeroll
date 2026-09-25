import CryptoKit
import Foundation

/// Photo-sized and video-sized files of random bytes, new content every time, so the PC gets new files on every
/// run. Files with the same names are overwritten. Blocking: call off the main thread.
enum TestFileFactory {
    static func create() throws -> [SpikeFile] {
        let folder = try FileManager.default
            .url(for: .applicationSupportDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            .appendingPathComponent(SpikeConstants.testFolder, isDirectory: true)
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)
        var files: [SpikeFile] = []
        for index in 1...SpikeConstants.testPhotoCount {
            let url = folder.appendingPathComponent(String(format: "test-photo-%02d.jpg", index))
            files.append(try write(url, size: SpikeConstants.testPhotoBytes))
        }
        files.append(try write(folder.appendingPathComponent("test-video.mp4"), size: SpikeConstants.testVideoBytes))
        return files
    }

    /// The SHA-256 is counted while writing, so the files need no second pass.
    private static func write(_ url: URL, size: Int) throws -> SpikeFile {
        FileManager.default.createFile(atPath: url.path, contents: nil)
        let handle = try FileHandle(forWritingTo: url)
        defer { try? handle.close() }
        var hasher = SHA256()
        var buffer = [UInt8](repeating: 0, count: SpikeConstants.chunkBytes)
        var left = size
        while left > 0 {
            let count = min(left, buffer.count)
            buffer.withUnsafeMutableBytes { arc4random_buf($0.baseAddress, count) }
            let chunk = Data(buffer[0..<count])
            try handle.write(contentsOf: chunk)
            hasher.update(data: chunk)
            left -= count
        }
        let sha256 = hasher.finalize().map { String(format: "%02x", $0) }.joined()
        return SpikeFile(name: url.lastPathComponent, url: url, size: Int64(size), sha256: sha256)
    }
}
