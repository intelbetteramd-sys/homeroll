import Foundation

/// "12.3 МБ".
func megabytes(_ bytes: Int64) -> String {
    String(format: "%.1f МБ", Double(bytes) / 1_048_576)
}

/// "23.4 МБ/с", or "—" when no time passed.
func speed(_ bytes: Int64, from start: Date?, to end: Date?) -> String {
    guard let start = start, let end = end, end > start else { return "—" }
    return String(format: "%.1f МБ/с", Double(bytes) / 1_048_576 / end.timeIntervalSince(start))
}

/// "ab12 cd34 …": a fingerprint split into groups of four, easy to compare with the PC window.
func groupFingerprint(_ hex: String) -> String {
    stride(from: 0, to: hex.count, by: 4).map { start -> String in
        let from = hex.index(hex.startIndex, offsetBy: start)
        let to = hex.index(from, offsetBy: 4, limitedBy: hex.endIndex) ?? hex.endIndex
        return String(hex[from..<to])
    }.joined(separator: " ")
}
