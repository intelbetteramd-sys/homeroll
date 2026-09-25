package app.pixroost.core.spike.oauth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.io.IOException

/**
 * The whole Picker round: create a session, open Google's page, poll until the user taps "Done", download the
 * picked files and delete the session. [openUrl] gets the raw picker link: Android hands it to the Google Photos
 * app, the desktop adds `/autoclose` and opens the system browser.
 */
class GooglePickerFlow(
    private val picker: GooglePhotosPicker,
    private val scope: CoroutineScope,
    private val accessToken: () -> String?,
    private val openUrl: (String) -> Unit,
    private val log: (String) -> Unit,
    private val nowMillis: () -> Long,
) {
    private val _status = MutableStateFlow("не запускался")
    val status: StateFlow<String> = _status.asStateFlow()

    fun start() {
        scope.launch {
            try {
                pickAndDownload(accessToken() ?: throw OAuthException("сначала войдите в Google"))
            } catch (error: IOException) {
                fail(error)
            } catch (error: OAuthException) {
                fail(error)
            }
        }
    }

    private suspend fun pickAndDownload(token: String) {
        var session = picker.createSession(token)
        _status.value = "выберите фото на странице Google и нажмите «Готово»"
        openUrl(session.pickerUri)
        val deadline = nowMillis() + OAuthSpikeConstants.SIGN_IN_TIMEOUT_MILLIS
        while (!session.isMediaItemsSet && nowMillis() < deadline) {
            delay(session.pollIntervalMillis)
            session = picker.getSession(token, session.id)
        }
        if (!session.isMediaItemsSet) throw OAuthException("выбор не завершён за 5 минут")
        val picked = picker.listPicked(token, session.id)
        _status.value = "выбрано ${picked.size}, скачиваю…"
        val startedAt = nowMillis()
        val bytes = picked.sumOf { picker.downloadSize(token, it) }
        picker.deleteSession(token, session.id)
        val millis = nowMillis() - startedAt
        _status.value =
            "скачано ${picked.size} файлов, ${bytes / OAuthSpikeConstants.BYTES_IN_KILOBYTE} КБ за $millis мс"
        log("Google Фото: ${picked.joinToString { "${it.fileName} (${it.mimeType})" }}")
    }

    private fun fail(error: Exception) {
        _status.value = "ошибка: ${error.message}"
        log("Google Фото: ошибка ${error.message}")
    }
}
