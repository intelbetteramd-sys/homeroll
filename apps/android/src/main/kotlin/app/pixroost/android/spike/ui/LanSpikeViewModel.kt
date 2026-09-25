package app.pixroost.android.spike.ui

import android.app.Application
import android.net.nsd.NsdManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pixroost.android.spike.data.DiscoveryResponder
import app.pixroost.android.spike.data.FoundPc
import app.pixroost.android.spike.data.LanDataConstants
import app.pixroost.android.spike.data.PcDiscovery
import app.pixroost.android.spike.data.PhoneAnnouncer
import app.pixroost.android.spike.data.PhoneServer
import app.pixroost.android.spike.data.connectToPc
import app.pixroost.android.spike.data.localIpv4Addresses
import app.pixroost.android.spike.ui.model.DiscoverRequests
import app.pixroost.android.spike.ui.model.LanEvent
import app.pixroost.android.spike.ui.model.LanSpikeUiState
import app.pixroost.android.spike.ui.model.PcFinding
import app.pixroost.core.spike.lan.LanSpikeConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

/** Runs both paths on the phone: finding the PC (direct) and waiting for the PC (reversed). */
class LanSpikeViewModel(application: Application) : AndroidViewModel(application) {
    private val startedAt = SystemClock.elapsedRealtime()
    private val phoneName = Build.MODEL
    private val nsd = application.getSystemService(NsdManager::class.java)
    private val multicastLock = application.getSystemService(WifiManager::class.java)
        .createMulticastLock(LanDataConstants.MULTICAST_LOCK_TAG)
        .apply {
            setReferenceCounted(false)
            acquire()
        }
    private val pcDiscovery = PcDiscovery(nsd)
    private val phoneServer = PhoneServer(phoneName)
    private val responder = DiscoveryResponder(phoneName)
    private val announcer = PhoneAnnouncer(nsd)

    private val _uiState = MutableStateFlow(LanSpikeUiState(phoneName = phoneName, addresses = localIpv4Addresses()))
    val uiState: StateFlow<LanSpikeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { runServer() }
        viewModelScope.launch { responder.run(::onDiscoverRequest, ::log) }
        announcer.start(phoneName, ::log)
        pcDiscovery.start(::onPcFound, ::log)
    }

    /** Starts looking for the PC again, with a fresh timer. */
    fun rediscover() {
        pcDiscovery.stop()
        _uiState.update { it.copy(pcs = emptyList()) }
        pcDiscovery.start(::onPcFound, ::log)
    }

    fun reconnect() = _uiState.value.pcs.forEach { connect(it.pc) }

    override fun onCleared() {
        pcDiscovery.stop()
        announcer.stop()
        phoneServer.close()
        responder.close()
        multicastLock.release()
    }

    private suspend fun runServer() {
        try {
            _uiState.update { it.copy(serverStatus = "ждёт ПК на порту ${LanSpikeConstants.PHONE_PORT}") }
            phoneServer.run { from, message ->
                log("ПК $from подключился к телефону: $message")
                _uiState.update { it.copy(pcGreetings = it.pcGreetings + "${elapsed()} мс — $from: $message") }
            }
        } catch (error: IOException) {
            _uiState.update { it.copy(serverStatus = "ошибка: ${error.message}") }
        }
    }

    private fun onDiscoverRequest(from: String) {
        val known = _uiState.value.discoverRequests.any { it.host == from }
        if (!known) log("ПК $from спросил «кто тут?», телефон ответил")
        _uiState.update { state ->
            val requests = if (known) {
                state.discoverRequests.map { if (it.host == from) it.copy(count = it.count + 1) else it }
            } else {
                state.discoverRequests + DiscoverRequests(from, count = 1, firstAfterMillis = elapsed())
            }
            state.copy(discoverRequests = requests)
        }
    }

    private fun onPcFound(pc: FoundPc) {
        log("Адрес ПК: ${pc.host}:${pc.port} за ${pc.resolvedAfterMillis} мс")
        _uiState.update { it.copy(pcs = it.pcs + PcFinding(pc, result = null)) }
        connect(pc)
    }

    private fun connect(pc: FoundPc) {
        viewModelScope.launch {
            val result = connectToPc(pc.host, pc.port, phoneName)
            log(
                if (result.isSuccess) {
                    "Телефон подключился к ПК: ${result.connectMillis} мс, ответ за ${result.answerMillis} мс"
                } else {
                    "Телефон не подключился к ПК: ${result.error}"
                },
            )
            _uiState.update { state ->
                state.copy(pcs = state.pcs.map { if (it.pc == pc) it.copy(result = result) else it })
            }
        }
    }

    private fun log(text: String) {
        _uiState.update {
            it.copy(log = (it.log + LanEvent(elapsed(), text)).takeLast(LanSpikeConstants.MAX_LOG_ENTRIES))
        }
    }

    private fun elapsed() = SystemClock.elapsedRealtime() - startedAt
}
