package app.pixroost.desktop.spike.ui

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.desktop.spike.ui.model.LanEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The last events with milliseconds since the app started. */
class EventLog {
    private val startedAt = System.currentTimeMillis()
    private val _entries = MutableStateFlow<List<LanEvent>>(emptyList())
    val entries: StateFlow<List<LanEvent>> = _entries.asStateFlow()

    fun add(text: String) {
        _entries.update { (it + LanEvent(elapsed(), text)).takeLast(LanSpikeConstants.MAX_LOG_ENTRIES) }
    }

    fun elapsed(): Long = System.currentTimeMillis() - startedAt
}
