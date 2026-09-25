import UIKit

/// Plain-text report of the iPhone side to paste into the spike's issue.
@MainActor
enum TransferReport {
    static func build(_ model: TransferSpikeModel) -> String {
        let device = UIDevice.current
        var lines = ["Pixroost S-03, iPhone", "Устройство: \(device.model), \(device.systemName) \(device.systemVersion)"]
        lines.append("ПК: \(model.pcName ?? "—"), адрес \(model.host ?? "не найден")")
        lines.append("Сертификат ПК: \(model.pinned.map(groupFingerprint) ?? "не запомнен")")
        lines.append("Итоги:")
        let transfers = Array(model.transfers.values).sorted { $0.file.name < $1.file.name }
        let batches = TransferSummary.batches(transfers)
        lines += (batches.isEmpty ? ["готовых файлов нет"] : batches).map { "  " + $0 }
        lines.append("Файлы:")
        lines += transfers.map { "  " + TransferSummary.line($0) }
        lines.append("Журнал:")
        lines += model.log.map { String(format: "  %.1f с  ", $0.seconds) + $0.text }
        return lines.joined(separator: "\n")
    }
}
