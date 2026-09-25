import Foundation

/// Uploads through a background URLSession: the system keeps sending while the app is suspended and wakes
/// the app when uploads finish. The PC certificate is checked against the pin here too.
final class UploadCenter: NSObject, URLSessionDataDelegate {
    static let shared = UploadCenter()

    /// The upload id (the file's SHA-256) and the bytes sent by this request so far.
    var onProgress: ((String, Int64) -> Void)?

    /// The upload id, whether the PC has the whole file, and why not when it does not.
    var onFinish: ((String, Bool, String?) -> Void)?

    /// iOS hands this over when it wakes the app for finished background uploads.
    var backgroundCompletion: (() -> Void)?

    private let pins = PinStore()

    /// Touched only on the session's own serial delegate queue.
    private var reported: [Int: Int64] = [:]

    lazy var session: URLSession = {
        let configuration = URLSessionConfiguration.background(withIdentifier: SpikeConstants.backgroundSessionId)
        configuration.isDiscretionary = false
        configuration.sessionSendsLaunchEvents = true
        return URLSession(configuration: configuration, delegate: self, delegateQueue: nil)
    }()

    /// Background sessions send only whole files, so a resumed upload sends a file with the rest.
    func upload(_ request: URLRequest, file: URL, id: String) {
        let task = session.uploadTask(with: request, fromFile: file)
        task.taskDescription = id
        task.resume()
    }

    func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void
    ) {
        let (disposition, credential) = answerPinned(challenge, pins: pins)
        completionHandler(disposition, credential)
    }

    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didSendBodyData bytesSent: Int64,
        totalBytesSent: Int64,
        totalBytesExpectedToSend: Int64
    ) {
        let last = reported[task.taskIdentifier] ?? 0
        guard totalBytesSent - last >= SpikeConstants.progressStepBytes || totalBytesSent == totalBytesExpectedToSend
        else { return }
        reported[task.taskIdentifier] = totalBytesSent
        onProgress?(task.taskDescription ?? "", totalBytesSent)
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        reported[task.taskIdentifier] = nil
        let response = task.response as? HTTPURLResponse
        let isComplete = error == nil && response?.value(forHTTPHeaderField: SpikeConstants.headerComplete) == "1"
        let reason = error?.localizedDescription ?? response.map { "ПК ответил \($0.statusCode)" }
        onFinish?(task.taskDescription ?? "", isComplete, isComplete ? nil : reason)
    }

    func urlSessionDidFinishEvents(forBackgroundURLSession session: URLSession) {
        DispatchQueue.main.async {
            self.backgroundCompletion?()
            self.backgroundCompletion = nil
        }
    }
}
