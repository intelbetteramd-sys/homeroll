package app.pixroost.android.spike.data

enum class FileStatus(val label: String) {
    Queued("в очереди"),
    Hashing("считаю SHA-256"),
    Sending("отправляется"),
    Retrying("связь оборвалась, повторяю"),
    Done("готово"),
    Duplicate("уже на ПК"),
    Failed("ошибка"),
}
