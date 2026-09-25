import SwiftUI

/// Navigation is native SwiftUI so the tab bar and toolbars get the system Liquid Glass on iOS 26;
/// screen content comes from shared Compose code (docs/architecture/adr/0009-liquid-glass-native-navigation.md).
/// Titles are translated in Localizable.xcstrings.
struct RootView: View {
    var body: some View {
        if #available(iOS 18.0, *) {
            TabView {
                Tab("Library", systemImage: "photo.on.rectangle") { section("Library") }
                Tab("Storage", systemImage: "square.stack.3d.up") { section("Storage") }
                Tab("Cleanup", systemImage: "sparkles") { section("Cleanup") }
                Tab("Devices", systemImage: "laptopcomputer") { section("Devices") }
                // On iOS 26 the search tab is a separate glass button next to the tab bar.
                Tab(role: .search) { SearchSection() }
            }
        } else {
            // iOS 16 and 17 have no search role, so the tab bar has only the four sections.
            TabView {
                section("Library").tabItem { Label("Library", systemImage: "photo.on.rectangle") }
                section("Storage").tabItem { Label("Storage", systemImage: "square.stack.3d.up") }
                section("Cleanup").tabItem { Label("Cleanup", systemImage: "sparkles") }
                section("Devices").tabItem { Label("Devices", systemImage: "laptopcomputer") }
            }
        }
    }

    private func section(_ title: LocalizedStringKey) -> some View {
        NavigationStack {
            ComposeScreen()
                .ignoresSafeArea()
                .navigationTitle(title)
        }
    }
}

private struct SearchSection: View {
    @State private var query = ""

    var body: some View {
        NavigationStack {
            ComposeScreen()
                .ignoresSafeArea()
                .navigationTitle("Search")
        }
        .searchable(text: $query)
    }
}
