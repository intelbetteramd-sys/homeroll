import SwiftUI

@main
struct PixroostApp: App {
    // Spike S-03: the transfer screen instead of RootView; background uploads wake the app through the delegate.
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            TransferSpikeView()
        }
    }
}
