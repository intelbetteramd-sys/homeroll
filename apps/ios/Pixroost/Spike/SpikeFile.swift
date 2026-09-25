import Foundation

/// A file to send and its SHA-256, which is also the upload id on the PC.
struct SpikeFile: Identifiable, Hashable {
    let name: String
    let url: URL
    let size: Int64
    let sha256: String

    var id: String { sha256 }
}
