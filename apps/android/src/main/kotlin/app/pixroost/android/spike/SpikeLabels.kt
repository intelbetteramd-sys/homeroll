package app.pixroost.android.spike

fun accessLabel(access: MediaAccess): String = when (access) {
    MediaAccess.Full -> "Доступ: все фото и видео"
    MediaAccess.Partial -> "Доступ: только выбранные (Android 14+)"
    MediaAccess.None -> "Нет доступа к фото"
}

fun scanLabel(scan: ScanResult): String {
    val videos = scan.items.count { it.isVideo }
    val generation = when {
        scan.generation == null -> "поколений нет (Android до 11)"
        scan.storeVersionChanged -> "версия MediaStore сменилась, нужен полный пересчёт"
        scan.changedSinceLastScan == null -> "поколение ${scan.generation}, первое сканирование"
        else -> "поколение ${scan.generation}, изменилось с прошлого сканирования: ${scan.changedSinceLastScan}"
    }
    return "Галерея: ${scan.items.size.formatted()} (видео ${videos.formatted()}). " +
        "Запрос ${scan.queryMillis} мс, чтение ${scan.readMillis} мс, " +
        "первые ${SpikeConstants.FIRST_PAGE_SIZE} — ${scan.firstPageMillis} мс. $generation"
}

fun gridLabel(cells: Int, stats: FrameStats): String = "Сетка: ${cells.formatted()} ячеек. ${statsLabel(stats)}"

fun statsLabel(stats: FrameStats): String = "Кадров ${stats.frames.formatted()}, с рывками ${stats.janky} " +
    "(${stats.jankyPercent.oneDecimal()} %), p50 ${stats.p50Millis.oneDecimal()} мс, " +
    "p90 ${stats.p90Millis.oneDecimal()} мс, p99 ${stats.p99Millis.oneDecimal()} мс, " +
    "макс ${stats.maxMillis.oneDecimal()} мс"

fun autoScrollLabel(result: AutoScrollResult): String =
    "Автопрокрутка ${result.seconds.oneDecimal()} с, пройдено ${result.itemsPassed.formatted()} ячеек. " +
        statsLabel(result.stats)
