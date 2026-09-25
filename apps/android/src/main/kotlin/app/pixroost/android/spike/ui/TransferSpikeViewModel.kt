package app.pixroost.android.spike.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pixroost.android.spike.data.DiscoveryResponder
import app.pixroost.android.spike.data.FileSource
import app.pixroost.android.spike.data.FileTransfer
import app.pixroost.android.spike.data.FoundPc
import app.pixroost.android.spike.data.LanDataConstants
import app.pixroost.android.spike.data.PinningTrust
import app.pixroost.android.spike.data.ReverseRelay
import app.pixroost.android.spike.data.TransferRunner
import app.pixroost.android.spike.data.UploadClient
import app.pixroost.android.spike.data.UploadRoute
import app.pixroost.android.spike.data.createTestFiles
import app.pixroost.android.spike.data.localIpv4Addresses
import app.pixroost.android.spike.ui.model.LanEvent
import app.pixroost.android.spike.ui.model.TransferSpikeUiState
import app.pixroost.android.spike.util.formatFingerprint
import app.pixroost.core.spike.lan.LanSpikeConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Finds the PC from its broadcast and sends files to it by the direct or the reversed path. */
class TransferSpikeViewModel(application: Application) : AndroidViewModel(application) {
    private val startedAt = SystemClock.elapsedRealtime()
    private val multicastLock = application.getSystemService(WifiManager::class.java)
        .createMulticastLock(LanDataConstants.MULTICAST_LOCK_TAG)
        .apply {
            setReferenceCounted(false)
            acquire()
        }
    private val source = FileSource(application)
    private val preferences = application.getSharedPreferences(LanDataConstants.PREFERENCES, Context.MODE_PRIVATE)
    private val trust: PinningTrust = PinningTrust(preferences) {
        log("Запомнил сертификат ПК: ${formatFingerprint(it)}")
        _uiState.update { state -> state.copy(pinned = it) }
    }
    private val client = UploadClient(trust, source)
    private val relay = ReverseRelay(::log)
    private val responder = DiscoveryResponder(Build.MODEL)
    private val runner = TransferRunner(client, source, ::baseUrl, ::log)
    private var transferJob: Job? = null

    private val _uiState: MutableStateFlow<TransferSpikeUiState> = MutableStateFlow(
        TransferSpikeUiState(phoneName = Build.MODEL, addresses = localIpv4Addresses(), pinned = trust.pinned),
    )
    val uiState: StateFlow<TransferSpikeUiState> = _uiState.asStateFlow()
    val transfers: StateFlow<List<FileTransfer>> = runner.transfers
    val waitingConnections: StateFlow<Int> = relay.waitingCount

    init {
        viewModelScope.launch { relay.run() }
        viewModelScope.launch { responder.run(::onPcRequest, ::log) }
    }

    fun selectRoute(route: UploadRoute) = _uiState.update { it.copy(route = route) }

    fun prepareTestFiles() {
        _uiState.update { it.copy(isPreparing = true) }
        viewModelScope.launch {
            val folder = File(getApplication<Application>().filesDir, LanDataConstants.TEST_FOLDER)
            val files = withContext(Dispatchers.IO) { createTestFiles(folder) }
            log("Созданы тестовые файлы: ${files.size}")
            _uiState.update { it.copy(files = files, isPreparing = false) }
        }
    }

    fun addPicked(uris: List<Uri>) {
        viewModelScope.launch {
            val files = withContext(Dispatchers.IO) { uris.mapNotNull(source::describe) }
            log("Выбрано в галерее: ${files.size}")
            _uiState.update { it.copy(files = files) }
        }
    }

    fun start() {
        val state = _uiState.value
        if (state.files.isEmpty() || transferJob?.isActive == true) return
        log("Отправляю ${state.files.size} файлов, путь ${state.route.label}")
        _uiState.update { it.copy(isRunning = true) }
        transferJob = viewModelScope.launch {
            try {
                runner.run(state.files, state.route)
                log("Отправка закончена")
            } finally {
                _uiState.update { it.copy(isRunning = false) }
            }
        }
    }

    fun stop() {
        transferJob?.cancel()
        log("Отправка остановлена")
    }

    /** For a PC whose certificate changed: the next connection pins the new one. */
    fun forgetPin() {
        trust.forget()
        _uiState.update { it.copy(pinned = null) }
        log("Сертификат ПК забыт")
    }

    override fun onCleared() {
        responder.close()
        relay.close()
        client.close()
        multicastLock.release()
    }

    private fun onPcRequest(host: String, pcName: String) {
        if (_uiState.value.pc?.host == host) return
        val pc = FoundPc(pcName, host, SystemClock.elapsedRealtime() - startedAt)
        log("ПК $pcName ($host) спросил «кто тут?», телефон ответил")
        _uiState.update { it.copy(pc = pc) }
    }

    private fun baseUrl(route: UploadRoute): String? = when (route) {
        UploadRoute.Direct -> _uiState.value.pc?.let { "https://${it.host}:${LanSpikeConstants.PC_PORT}" }
        UploadRoute.Reversed -> relay.localPort.takeIf { it > 0 }?.let { "https://${LanDataConstants.LOOPBACK}:$it" }
    }

    private fun log(text: String) {
        val event = LanEvent(SystemClock.elapsedRealtime() - startedAt, text)
        _uiState.update { it.copy(log = (it.log + event).takeLast(LanSpikeConstants.MAX_LOG_ENTRIES)) }
    }
}
