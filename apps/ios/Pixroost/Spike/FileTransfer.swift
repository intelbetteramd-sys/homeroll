import Foundation

/// One file on its way to the PC. An attempt is one upload of the rest of the file.
struct FileTransfer {
    let file: SpikeFile
    var status: TransferStatus = .queued
    var sent: Int64 = 0
    var attempts = 0
    var resumedFrom: [Int64] = []
    var firstAttemptAt: Date?
    var attemptStartedAt: Date?
    var attemptStartOffset: Int64 = 0
    var finishedAt: Date?
    var error: String?
    let deadline = Date().addingTimeInterval(SpikeConstants.retryWindowSeconds)
}
