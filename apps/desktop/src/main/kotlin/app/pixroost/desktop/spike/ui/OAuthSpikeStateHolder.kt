package app.pixroost.desktop.spike.ui

import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.desktop.spike.ui.model.LogLine
import app.pixroost.desktop.spike.ui.model.OAuthSpikeUiState
import app.pixroost.desktop.spike.ui.model.ServiceUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The window's state: the service cards and the event log with the time since start. */
class OAuthSpikeStateHolder(tokenStorage: String) {
    private val startedAt = System.currentTimeMillis()
    private val _state = MutableStateFlow(OAuthSpikeUiState(tokenStorage = tokenStorage))
    val state: StateFlow<OAuthSpikeUiState> = _state.asStateFlow()

    fun change(service: CloudService, transform: (ServiceUiState) -> ServiceUiState) =
        _state.update { it.copy(services = it.services + (service to transform(it.services.getValue(service)))) }

    fun log(text: String) = _state.update {
        val line = LogLine(System.currentTimeMillis() - startedAt, text)
        it.copy(log = (it.log + line).takeLast(UiConstants.MAX_LOG_LINES))
    }

    fun fail(service: CloudService, error: Exception) {
        change(service) { it.copy(status = "ошибка: ${error.message}") }
        log("${service.label}: ошибка ${error.message}")
    }
}
