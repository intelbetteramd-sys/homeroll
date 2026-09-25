package app.pixroost.desktop.spike.report

import app.pixroost.desktop.spike.ui.model.PhoneFinding

/** "Redmi (192.168.1.5), рассылка за 850 мс → подключился за 12 мс, ответ за 20 мс". */
fun phoneFindingLabel(finding: PhoneFinding): String {
    val phone = finding.phone
    val found = "${phone.name} (${phone.host}), ${phone.method.label} за ${phone.foundAfterMillis} мс"
    val result = finding.result
    val connection = when {
        result == null -> "подключаюсь…"
        result.isSuccess -> "подключился за ${result.connectMillis} мс, ответ за ${result.answerMillis} мс"
        else -> "не подключился: ${result.error}"
    }
    return "$found → $connection"
}
