package app.pixroost.desktop.spike.ui

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.desktop.spike.data.FoundPhone
import app.pixroost.desktop.spike.data.LanDataConstants
import app.pixroost.desktop.spike.data.LocalAddress
import app.pixroost.desktop.spike.data.PcAnnouncer
import app.pixroost.desktop.spike.data.PhoneBroadcastFinder
import app.pixroost.desktop.spike.data.ReceivedUpload
import app.pixroost.desktop.spike.data.ReverseLinkStats
import app.pixroost.desktop.spike.data.ReverseLinks
import app.pixroost.desktop.spike.data.ServerCertificate
import app.pixroost.desktop.spike.data.TransferServer
import app.pixroost.desktop.spike.data.TransferStore
import app.pixroost.desktop.spike.data.UploadHandler
import app.pixroost.desktop.spike.data.WindowsFirewall
import app.pixroost.desktop.spike.data.localIpv4Addresses
import app.pixroost.desktop.spike.ui.model.LanEvent
import app.pixroost.desktop.spike.ui.model.TransferSpikeUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.net.InetAddress
import java.security.GeneralSecurityException

/** State holder of the S-03 window: the HTTPS server, the reversed links, the firewall and the log. */
class TransferSpikeController(private val scope: CoroutineScope) {
    private val eventLog = EventLog()
    private val pcName = System.getenv("COMPUTERNAME") ?: InetAddress.getLocalHost().hostName
    private val firewall = WindowsFirewall(ProcessHandle.current().info().command().orElse("java"))
    private val store = TransferStore(File(System.getProperty("user.home"), LanDataConstants.SPIKE_FOLDER))
    private val handler = UploadHandler(store, pcName, eventLog::add)
    private val links = ReverseLinks(scope, eventLog::add)
    private var server: TransferServer? = null
    private var announcer: PcAnnouncer? = null
    private var broadcastJob: Job? = null

    private val _state = MutableStateFlow(TransferSpikeUiState(pcName = pcName, folder = store.root.path))
    val state: StateFlow<TransferSpikeUiState> = _state.asStateFlow()
    val log: StateFlow<List<LanEvent>> = eventLog.entries
    val uploads: StateFlow<Map<String, ReceivedUpload>> = handler.uploads
    val linkStats: StateFlow<Map<String, ReverseLinkStats>> = links.stats

    fun start() {
        val addresses = localIpv4Addresses()
        _state.update { state -> state.copy(addresses = addresses.map { it.label() }) }
        eventLog.add("Адреса ПК: ${addresses.joinToString { it.address.hostAddress }}")
        scope.launch(Dispatchers.IO) { startServer() }
        scope.launch(Dispatchers.IO) { announce(addresses.firstOrNull()?.address) }
        startBroadcastFinder(addresses)
        refreshFirewall()
    }

    /** Reads the adapters again and looks for phones with fresh reversed connections. */
    fun rediscover() {
        val addresses = localIpv4Addresses()
        eventLog.add("Ищу телефон заново, адреса ПК: ${addresses.joinToString { it.address.hostAddress }}")
        _state.update { state -> state.copy(addresses = addresses.map { it.label() }, phones = emptyList()) }
        broadcastJob?.cancel()
        links.closeAll()
        startBroadcastFinder(addresses)
    }

    fun refreshFirewall() {
        scope.launch(Dispatchers.IO) { _state.update { it.copy(firewall = firewall.state()) } }
    }

    /** Adds the rule an installer would add, or removes every rule for Java. Windows asks for admin rights. */
    fun setRule(enabled: Boolean) {
        scope.launch(Dispatchers.IO) {
            eventLog.add(
                if (enabled) "Добавляю правило «${LanDataConstants.RULE_NAME}»" else "Удаляю все правила для Java",
            )
            if (enabled) firewall.addRule() else firewall.removeRules()
            _state.update { it.copy(firewall = firewall.state()) }
        }
    }

    fun close() {
        server?.stop()
        announcer?.close()
        links.closeAll()
        scope.cancel()
    }

    private fun startServer() {
        try {
            val certificate = ServerCertificate(File(store.root, LanDataConstants.KEYSTORE_FILE))
            server = TransferServer(certificate, handler).also { it.start() }
            _state.update {
                it.copy(
                    serverStatus = "HTTPS, порт ${LanSpikeConstants.PC_PORT}",
                    fingerprint = certificate.fingerprint,
                )
            }
            eventLog.add("Сервер HTTPS запущен на порту ${LanSpikeConstants.PC_PORT}")
        } catch (error: IOException) {
            serverFailed(error)
        } catch (error: GeneralSecurityException) {
            serverFailed(error)
        }
    }

    private fun serverFailed(error: Exception) {
        _state.update { it.copy(serverStatus = "ошибка: ${error.message}") }
        eventLog.add("Сервер не запустился: ${error.message}")
    }

    private fun announce(address: InetAddress?) {
        if (address == null) {
            _state.update { it.copy(announceStatus = "нет адреса в локальной сети") }
            return
        }
        try {
            announcer = PcAnnouncer(address, pcName)
            _state.update { it.copy(announceStatus = "объявлен на ${address.hostAddress} (для iPhone)") }
        } catch (error: IOException) {
            _state.update { it.copy(announceStatus = "ошибка: ${error.message}") }
        }
    }

    private fun onPhoneFound(phone: FoundPhone) {
        eventLog.add("Найден телефон ${phone.name} (${phone.host}) за ${phone.foundAfterMillis} мс")
        _state.update { it.copy(phones = it.phones + phone) }
        links.open(phone.host, phone.port)
    }

    private fun startBroadcastFinder(addresses: List<LocalAddress>) {
        val startedAt = System.currentTimeMillis()
        val targets = listOf(InetAddress.getByName(LanDataConstants.BROADCAST_ALL)) +
            addresses.mapNotNull { it.broadcast }
        val finder = PhoneBroadcastFinder(pcName, onError = eventLog::add)
        broadcastJob = scope.launch { finder.run(targets, startedAt, ::onPhoneFound) }
    }

    private fun LocalAddress.label() = "$interfaceName: ${address.hostAddress}"
}
