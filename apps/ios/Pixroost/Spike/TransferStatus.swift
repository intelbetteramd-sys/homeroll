enum TransferStatus: String {
    case queued = "в очереди"
    case offering = "спрашиваю ПК"
    case sending = "отправляется"
    case retrying = "связь оборвалась, повторяю"
    case done = "готово"
    case duplicate = "уже на ПК"
    case failed = "ошибка"

    var isFinal: Bool { self == .done || self == .duplicate || self == .failed }
}
