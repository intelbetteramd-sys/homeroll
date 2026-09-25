import Foundation

/// Delegate of the foreground session that asks the PC how much of a file it has.
final class TrustDelegate: NSObject, URLSessionDelegate {
    private let pins: PinStore

    init(pins: PinStore) {
        self.pins = pins
    }

    func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void
    ) {
        let (disposition, credential) = answerPinned(challenge, pins: pins)
        completionHandler(disposition, credential)
    }
}
