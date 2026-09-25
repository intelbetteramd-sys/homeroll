import SwiftUI
import UIKit

/// Spike S-03 on the iPhone: finds the PC, sends test files with a background URLSession, keeps a log.
/// Send, then lock the phone or switch apps: the system keeps uploading.
struct TransferSpikeView: View {
    @StateObject private var model = TransferSpikeModel()
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        NavigationStack {
            List {
                pcSection
                filesSection
                sendSection
                logSection
            }
            .navigationTitle("Pixroost S-03")
            .toolbar {
                Button("Отчёт") { UIPasteboard.general.string = TransferReport.build(model) }
            }
        }
        .onAppear { model.start() }
        .onChange(of: scenePhase) { phase in model.phaseChanged(phase) }
    }

    private var pcSection: some View {
        Section("ПК") {
            Text(model.pcHost.map { "\(model.pcName ?? "ПК"): \($0)" } ?? "ищу по Bonjour…")
            TextField("Адрес ПК вручную, например 192.168.1.5", text: $model.manualHost)
                .keyboardType(.numbersAndPunctuation)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
            Text(model.pinned.map { "Сертификат: " + groupFingerprint($0) } ?? "Сертификат запомнится при первом подключении")
                .font(.footnote)
            if model.pinned != nil {
                Button("Забыть сертификат") { model.forgetPin() }
            }
        }
    }

    private var filesSection: some View {
        Section("Файлы") {
            Button(model.isPreparing ? "Создаю…" : "Создать тестовые: 20 × 3 МБ и 500 МБ") { model.prepareTestFiles() }
                .disabled(model.isPreparing)
            Text(model.files.isEmpty
                ? "не созданы"
                : "\(model.files.count) шт., \(megabytes(model.files.reduce(0) { $0 + $1.size }))")
        }
    }

    private var sendSection: some View {
        Section("Отправка") {
            Button("Отправить на ПК") { model.send() }
                .disabled(model.files.isEmpty || model.host == nil)
            if model.totalBytes > 0 {
                ProgressView(value: Double(model.sentBytes), total: Double(model.totalBytes))
            }
            let transfers = Array(model.transfers.values).sorted { $0.file.name < $1.file.name }
            ForEach(TransferSummary.batches(transfers), id: \.self) { Text($0).font(.subheadline) }
            ForEach(transfers.filter { $0.status != .done && $0.status != .queued }, id: \.file.sha256) {
                Text(TransferSummary.line($0)).font(.caption)
            }
        }
    }

    private var logSection: some View {
        Section("Журнал") {
            ForEach(model.log.reversed()) { line in
                Text(String(format: "%.1f с  ", line.seconds) + line.text).font(.caption2)
            }
        }
    }
}
