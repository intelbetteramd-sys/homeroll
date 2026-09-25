import Foundation
import SwiftUI

/// Finds the PC, asks it how much of each file it has and sends the rest with a background URLSession.
/// Retries every two seconds for five minutes after a break.
@MainActor
final class TransferSpikeModel: ObservableObject {
    @Published var manualHost = ""
    @Published private(set) var pcName: String?
    @Published private(set) var pcHost: String?
    @Published private(set) var pinned: String?
    @Published private(set) var files: [SpikeFile] = []
    @Published private(set) var transfers: [String: FileTransfer] = [:]
    @Published private(set) var isPreparing = false
    @Published private(set) var log: [LogLine] = []

    private let startedAt = Date()
    private let pins = PinStore()
    private let browser = PcBrowser()
    private lazy var offerSession = URLSession(
        configuration: .ephemeral,
        delegate: TrustDelegate(pins: pins),
        delegateQueue: nil
    )
    private var isStarted = false

    /// The address typed by hand wins over the one found by Bonjour.
    var host: String? {
        let typed = manualHost.trimmingCharacters(in: .whitespaces)
        return typed.isEmpty ? pcHost : typed
    }

    var totalBytes: Int64 { transfers.values.reduce(0) { $0 + $1.file.size } }
    var sentBytes: Int64 { transfers.values.reduce(0) { $0 + $1.sent } }

    func start() {
        guard !isStarted else { return }
        isStarted = true
        pinned = pins.pinned
        let center = UploadCenter.shared
        center.onProgress = { [weak self] id, sent in
            Task { @MainActor in self?.progress(id: id, sentByRequest: sent) }
        }
        center.onFinish = { [weak self] id, isComplete, reason in
            Task { @MainActor in self?.finished(id: id, isComplete: isComplete, reason: reason) }
        }
        _ = center.session
        browser.start { [weak self] name, host in
            Task { @MainActor in self?.found(name: name, host: host) }
        }
        record("Ищу ПК по Bonjour (\(SpikeConstants.serviceType))")
    }

    func prepareTestFiles() {
        isPreparing = true
        record("Создаю тестовые файлы…")
        Task.detached(priority: .userInitiated) {
            let result = Result { try TestFileFactory.create() }
            await self.testFilesCreated(result)
        }
    }

    func send() {
        guard let host = host else {
            record("Адрес ПК неизвестен")
            return
        }
        record("Отправляю \(files.count) файлов на \(host)")
        for file in files {
            transfers[file.sha256] = FileTransfer(file: file)
            Task { await offer(file) }
        }
    }

    func forgetPin() {
        pins.forget()
        pinned = nil
        record("Сертификат ПК забыт")
    }

    func phaseChanged(_ phase: ScenePhase) {
        switch phase {
        case .background: record("Приложение свёрнуто")
        case .active: record("Приложение открыто")
        default: break
        }
    }

    // MARK: - Uploads

    private func offer(_ file: SpikeFile) async {
        guard let host = host else { return }
        update(file.sha256) { $0.status = .offering }
        do {
            let (offset, isComplete) = try await requestOffer(file, host: host)
            if isComplete {
                markComplete(file)
                return
            }
            let body: URL
            if offset > 0 {
                body = try await Task.detached { try TailFile.make(from: file, offset: offset) }.value
            } else {
                body = file.url
            }
            try startUpload(file, host: host, offset: offset, body: body)
        } catch {
            retryLater(file, reason: error.localizedDescription)
        }
    }

    /// POST creates the upload or finds the one started before and answers with the PC's offset.
    private func requestOffer(_ file: SpikeFile, host: String) async throws -> (Int64, Bool) {
        var request = URLRequest(url: try uploadsURL(host))
        request.httpMethod = "POST"
        request.timeoutInterval = SpikeConstants.offerTimeoutSeconds
        request.setValue(file.sha256, forHTTPHeaderField: SpikeConstants.headerSha256)
        request.setValue(String(file.size), forHTTPHeaderField: SpikeConstants.headerLength)
        request.setValue(
            file.name.addingPercentEncoding(withAllowedCharacters: .alphanumerics) ?? file.sha256,
            forHTTPHeaderField: SpikeConstants.headerName
        )
        let (_, response) = try await offerSession.data(for: request)
        pinned = pins.pinned
        guard let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) else {
            throw SpikeError.status((response as? HTTPURLResponse)?.statusCode ?? 0)
        }
        let offset = Int64(http.value(forHTTPHeaderField: SpikeConstants.headerOffset) ?? "") ?? 0
        return (offset, http.value(forHTTPHeaderField: SpikeConstants.headerComplete) == "1")
    }

    private func startUpload(_ file: SpikeFile, host: String, offset: Int64, body: URL) throws {
        var request = URLRequest(url: try uploadsURL(host).appendingPathComponent(file.sha256))
        request.httpMethod = "PATCH"
        request.setValue(String(offset), forHTTPHeaderField: SpikeConstants.headerOffset)
        request.setValue("application/offset+octet-stream", forHTTPHeaderField: "Content-Type")
        let now = Date()
        update(file.sha256) { transfer in
            transfer.status = .sending
            transfer.sent = offset
            transfer.attempts += 1
            if offset > 0 { transfer.resumedFrom.append(offset) }
            transfer.firstAttemptAt = transfer.firstAttemptAt ?? now
            transfer.attemptStartedAt = now
            transfer.attemptStartOffset = offset
            transfer.error = nil
        }
        if offset > 0 { record("\(file.name): докачка с \(megabytes(offset))") }
        UploadCenter.shared.upload(request, file: body, id: file.sha256)
    }

    private func uploadsURL(_ host: String) throws -> URL {
        var components = URLComponents()
        components.scheme = "https"
        components.host = host
        components.port = SpikeConstants.pcPort
        guard let base = components.url else { throw SpikeError.badAddress(host) }
        return base.appendingPathComponent(SpikeConstants.uploadsPath)
    }

    private func progress(id: String, sentByRequest: Int64) {
        update(id) { $0.sent = $0.attemptStartOffset + sentByRequest }
    }

    private func finished(id: String, isComplete: Bool, reason: String?) {
        guard let transfer = transfers[id] else {
            record("Фоновая загрузка \(id.prefix(8)) закончилась: \(isComplete ? "готово" : reason ?? "")")
            return
        }
        guard isComplete else {
            retryLater(transfer.file, reason: reason ?? "ПК не подтвердил файл")
            return
        }
        update(id) { transfer in
            transfer.status = .done
            transfer.sent = transfer.file.size
            transfer.finishedAt = Date()
        }
        if transfers.values.allSatisfy({ $0.status.isFinal }) { record("Отправка закончена") }
    }

    /// The PC already has the whole file: sent before this run, or the answer to the last upload was lost.
    private func markComplete(_ file: SpikeFile) {
        update(file.sha256) { transfer in
            transfer.status = transfer.attempts == 0 ? .duplicate : .done
            transfer.sent = file.size
            transfer.finishedAt = Date()
        }
    }

    private func retryLater(_ file: SpikeFile, reason: String) {
        guard let transfer = transfers[file.sha256] else { return }
        guard Date() < transfer.deadline else {
            update(file.sha256) { $0.status = .failed; $0.error = reason }
            record("\(file.name): не отправлен, \(reason)")
            return
        }
        update(file.sha256) { $0.status = .retrying; $0.error = reason }
        record("\(file.name): \(reason), повтор через 2 с")
        Task {
            try? await Task.sleep(nanoseconds: SpikeConstants.retryDelayNanoseconds)
            await offer(file)
        }
    }

    // MARK: - State

    private func testFilesCreated(_ result: Result<[SpikeFile], Error>) {
        isPreparing = false
        switch result {
        case .success(let created):
            files = created
            transfers = [:]
            record("Созданы тестовые файлы: \(created.count), \(megabytes(created.reduce(0) { $0 + $1.size }))")
        case .failure(let error):
            record("Тестовые файлы не созданы: \(error.localizedDescription)")
        }
    }

    private func found(name: String, host: String) {
        guard pcHost != host else { return }
        pcName = name
        pcHost = host
        record("Найден ПК «\(name)»: \(host)")
    }

    private func update(_ id: String, _ change: (inout FileTransfer) -> Void) {
        guard var transfer = transfers[id] else { return }
        change(&transfer)
        transfers[id] = transfer
    }

    func record(_ text: String) {
        log.append(LogLine(seconds: Date().timeIntervalSince(startedAt), text: text))
        if log.count > SpikeConstants.maxLogLines { log.removeFirst(log.count - SpikeConstants.maxLogLines) }
    }
}
