import Foundation

/// The part of a file after the PC's offset, copied to a temporary file with a fixed name per upload:
/// a background upload sends a whole file, so a resumed one sends this. Blocking: call off the main thread.
enum TailFile {
    static func make(from file: SpikeFile, offset: Int64) throws -> URL {
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("\(file.sha256).tail")
        FileManager.default.createFile(atPath: url.path, contents: nil)
        let input = try FileHandle(forReadingFrom: file.url)
        let output = try FileHandle(forWritingTo: url)
        defer {
            try? input.close()
            try? output.close()
        }
        try input.seek(toOffset: UInt64(offset))
        while let chunk = try input.read(upToCount: SpikeConstants.chunkBytes), !chunk.isEmpty {
            try output.write(contentsOf: chunk)
        }
        return url
    }
}
