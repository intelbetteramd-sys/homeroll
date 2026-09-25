import Foundation

/// The pinned PC certificate. Until pairing by QR exists, the first certificate seen is trusted;
/// the screen shows it to compare with the PC window. UserDefaults is safe to use from any thread.
final class PinStore {
    private let defaults = UserDefaults.standard

    var pinned: String? { defaults.string(forKey: SpikeConstants.pinnedKey) }

    /// True when the certificate may be used. Pins it when nothing is pinned yet.
    func accept(_ fingerprint: String) -> Bool {
        if let pinned = pinned {
            return pinned == fingerprint
        }
        defaults.set(fingerprint, forKey: SpikeConstants.pinnedKey)
        return true
    }

    func forget() {
        defaults.removeObject(forKey: SpikeConstants.pinnedKey)
    }
}
