import Foundation

/// A line of the event log: seconds since the app started and what happened.
struct LogLine: Identifiable {
    let id = UUID()
    let seconds: Double
    let text: String
}
