import Foundation

/// Spike S-03: the same values as LanSpikeConstants and TransferSpikeConstants in the shared Kotlin code.
enum SpikeConstants {
    static let pcPort = 47200
    static let serviceType = "_pixroost._tcp"
    static let uploadsPath = "v1/uploads"

    static let headerOffset = "Upload-Offset"
    static let headerLength = "Upload-Length"
    static let headerSha256 = "Upload-Sha256"
    static let headerName = "Upload-Name"
    static let headerComplete = "Upload-Complete"

    static let backgroundSessionId = "app.pixroost.spike.s03.uploads"
    static let pinnedKey = "s03.pinnedFingerprint"
    static let testFolder = "s03-test"

    static let testPhotoCount = 20
    static let testPhotoBytes = 3 * 1024 * 1024
    static let testVideoBytes = 500 * 1024 * 1024
    static let largeFileBytes: Int64 = 50 * 1024 * 1024
    static let chunkBytes = 1024 * 1024
    static let progressStepBytes: Int64 = 1024 * 1024

    static let offerTimeoutSeconds: TimeInterval = 5
    static let retryDelayNanoseconds: UInt64 = 2_000_000_000
    static let retryWindowSeconds: TimeInterval = 300
    static let maxLogLines = 80
}
