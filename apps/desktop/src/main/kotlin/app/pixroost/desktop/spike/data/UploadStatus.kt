package app.pixroost.desktop.spike.data

enum class UploadStatus(val label: String) {
    Waiting("ждёт данных"),
    Receiving("принимается"),
    Interrupted("оборвалась, ждёт докачки"),
    Verifying("проверка SHA-256"),
    Done("готово, SHA-256 совпал"),
    Mismatch("SHA-256 не совпал"),
    Duplicate("уже в архиве"),
}
