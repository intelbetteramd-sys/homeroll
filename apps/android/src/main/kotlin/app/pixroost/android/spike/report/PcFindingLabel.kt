package app.pixroost.android.spike.report

import app.pixroost.android.spike.ui.model.PcFinding

/** "Pixroost HOME (192.168.1.10), найден за 900 мс, адрес за 950 мс → подключился за 15 мс". */
fun pcFindingLabel(finding: PcFinding): String {
    val pc = finding.pc
    val found = "${pc.name} (${pc.host}), найден за ${pc.foundAfterMillis} мс, адрес за ${pc.resolvedAfterMillis} мс"
    val result = finding.result
    val connection = when {
        result == null -> "подключаюсь…"
        result.isSuccess -> "подключился за ${result.connectMillis} мс, ответ за ${result.answerMillis} мс"
        else -> "не подключился: ${result.error}"
    }
    return "$found → $connection"
}
