import Foundation

/// Text lines for the screen and the report.
enum TransferSummary {
    /// Photos and large files separately: all bytes over the time from the first upload to the last finish.
    static func batches(_ transfers: [FileTransfer]) -> [String] {
        let done = transfers.filter { $0.status == .done }
        let groups = Dictionary(grouping: done) { $0.file.size >= SpikeConstants.largeFileBytes }
        return groups.keys.sorted { !$0 && $1 }.compactMap { isLarge -> String? in
            guard let group = groups[isLarge], !group.isEmpty else { return nil }
            let bytes = group.reduce(Int64(0)) { $0 + $1.file.size }
            let start = group.compactMap(\.firstAttemptAt).min()
            let end = group.compactMap(\.finishedAt).max()
            let seconds = start.flatMap { begin in end.map { $0.timeIntervalSince(begin) } } ?? 0
            let attempts = group.reduce(0) { $0 + $1.attempts }
            return "\(isLarge ? "Большие" : "Фото"): файлов \(group.count), \(megabytes(bytes)) за "
                + String(format: "%.1f с", seconds) + ", \(speed(bytes, from: start, to: end)), попыток \(attempts)"
        }
    }

    /// "test-video.mp4: 120.0 МБ из 500.0 МБ, отправляется; попыток 2, докачка с 80.0 МБ, 31.2 МБ/с".
    static func line(_ transfer: FileTransfer) -> String {
        var details: [String] = []
        if transfer.attempts > 0 { details.append("попыток \(transfer.attempts)") }
        details += transfer.resumedFrom.map { "докачка с \(megabytes($0))" }
        if transfer.attempts > 0 {
            let end = transfer.finishedAt ?? Date()
            let bytes = transfer.sent - transfer.attemptStartOffset
            details.append("последняя попытка \(speed(bytes, from: transfer.attemptStartedAt, to: end))")
        }
        if let error = transfer.error { details.append(error) }
        let head = "\(transfer.file.name): \(megabytes(transfer.sent)) из \(megabytes(transfer.file.size)), "
            + transfer.status.rawValue
        return details.isEmpty ? head : head + "; " + details.joined(separator: ", ")
    }
}
