import UIKit

/// iOS wakes the app when background uploads finish; the session must be recreated to deliver the events.
final class AppDelegate: NSObject, UIApplicationDelegate {
    func application(
        _ application: UIApplication,
        handleEventsForBackgroundURLSession identifier: String,
        completionHandler: @escaping () -> Void
    ) {
        UploadCenter.shared.backgroundCompletion = completionHandler
        _ = UploadCenter.shared.session
    }
}
