import PixroostKit
import SwiftUI
import UIKit

/// Hosts a screen written in shared Compose code.
struct ComposeScreen: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        PlaceholderViewControllerKt.PlaceholderViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
