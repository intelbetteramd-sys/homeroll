package app.pixroost.desktop.spike.ui

import app.pixroost.core.spike.lan.LanSpikeConstants
import app.pixroost.desktop.spike.data.FoundPhone
import app.pixroost.desktop.spike.data.LanDataConstants
import app.pixroost.desktop.spike.data.LocalAddress
import app.pixroost.desktop.spike.data.PcAnnouncer
import app.pixroost.desktop.spike.data.PcServer
import app.pixroost.desktop.spike.data.PhoneBroadcastFinder
import app.pixroost.desktop.spike.data.PhoneMdnsFinder
import app.pixroost.desktop.spike.data.WindowsFirewall
import app.pixroost.desktop.spike.data.connectToPhone
import app.pixroost.desktop.spike.data.localIpv4Addresses
import app.pixroost.desktop.spike.ui.model.LanEvent
import app.pixroost.desktop.spike.ui.model.LanSpikeUiState
import app.pixroost.desktop.spike.ui.model.PhoneFinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.InetAddress

/** State holder of the S-04 window: runs both paths and the firewall checks, exposes one UI state. */
class LanSpikeController(private val scope: CoroutineScope) {
    private val eventLog = EventLog()
    private val pcName = System.getenv("COMPUTERNAME") ?: InetAddress.getLocalHost().hostName
    private val firewall = WindowsFirewall(ProcessHandle.current().info().command().orElse("java"))
    private val server = PcServer(pcName)
    private var announcer: PcAnnouncer? = null
    private var mdnsFinder: PhoneMdnsFinder? = null
    private var broadcastJob: Job? = null

    private val _state = MutableStateFlow(LanSpikeUiState(pcName = pcName))
    val state: StateFlow<LanSpikeUiState> = _state.asStateFlow()
    val log: StateFlow<List<LanEvent>> = eventLog.entries

    fun start() {
        val addresses = localIpv4Addresses()
        _state.update { state ->
            state.copy(addresses = addresses.map { "${it.interfaceName}: ${it.address.hostAddress}" })
        }
        eventLog.add("Адреса ПК: ${addresses.joinToString { it.address.hostAddress }}")
        scope.launch { runServer() }
        scope.launch(Dispatchers.IO) { announce(addresses.firstOrNull()?.address) }
        startBroadcastFinder(addresses)
        refreshFirewall()
    }

    /**
     * Looks for the phone again with a fresh timer, e.g. after changing the firewall rules.
     * Reads the adapters again: a VPN or another Wi-Fi network changes the broadcast addresses.
     */
    fun rediscover() {
        val addresses = localIpv4Addresses()
        eventLog.add("Ищу телефон заново, адреса ПК: ${addresses.joinToString { it.address.hostAddress }}")
        _state.update { state ->
            state.copy(
                addresses = addresses.map { "${it.interfaceName}: ${it.address.hostAddress}" },
                phones = emptyList(),
            )
        }
        broadcastJob?.cancel()
        startBroadcastFinder(addresses)
        mdnsFinder?.let { finder ->
            finder.stop()
            finder.start(System.currentTimeMillis(), ::onPhoneFound)
        }
    }

    fun reconnect() = _state.value.phones.forEach { connect(it.phone) }

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
        server.close()
        mdnsFinder?.stop()
        announcer?.close()
        scope.cancel()
    }

    private suspend fun runServer() {
        try {
            _state.update { it.copy(serverStatus = "слушает порт ${LanSpikeConstants.PC_PORT}") }
            server.run { from, message ->
                eventLog.add("Телефон $from подключился к ПК: $message")
                _state.update { it.copy(greetings = it.greetings + "${eventLog.elapsed()} мс — $from: $message") }
            }
        } catch (error: IOException) {
            _state.update { it.copy(serverStatus = "ошибка: ${error.message}") }
            eventLog.add("Сервер ПК не запустился: ${error.message}")
        }
    }

    private fun announce(address: InetAddress?) {
        if (address == null) {
            _state.update { it.copy(announceStatus = "нет адреса в локальной сети") }
            return
        }
        try {
            val newAnnouncer = PcAnnouncer(address, pcName).also { announcer = it }
            _state.update { it.copy(announceStatus = "объявлен на ${address.hostAddress}") }
            eventLog.add("mDNS: ПК объявлен на ${address.hostAddress}")
            mdnsFinder =
                PhoneMdnsFinder(newAnnouncer.jmdns).also { it.start(System.currentTimeMillis(), ::onPhoneFound) }
        } catch (error: IOException) {
            _state.update { it.copy(announceStatus = "ошибка: ${error.message}") }
            eventLog.add("mDNS не запустился: ${error.message}")
        }
    }

    private fun onPhoneFound(phone: FoundPhone) {
        eventLog.add(
            "Найден телефон ${phone.name} (${phone.host}) через ${phone.method.label} за ${phone.foundAfterMillis} мс",
        )
        _state.update { it.copy(phones = it.phones + PhoneFinding(phone, result = null)) }
        connect(phone)
    }

    private fun connect(phone: FoundPhone) {
        scope.launch {
            val result = connectToPhone(phone.host, phone.port, pcName)
            eventLog.add(
                if (result.isSuccess) {
                    "ПК подключился к ${phone.host}: ${result.connectMillis} мс, ответ за ${result.answerMillis} мс"
                } else {
                    "ПК не подключился к ${phone.host}: ${result.error}"
                },
            )
            _state.update { state ->
                state.copy(phones = state.phones.map { if (it.phone == phone) it.copy(result = result) else it })
            }
        }
    }

    private fun startBroadcastFinder(addresses: List<LocalAddress>) {
        val startedAt = System.currentTimeMillis()
        val targets = listOf(InetAddress.getByName(LanDataConstants.BROADCAST_ALL)) +
            addresses.mapNotNull { it.broadcast }
        val finder = PhoneBroadcastFinder(pcName, onError = eventLog::add)
        broadcastJob = scope.launch { finder.run(targets, startedAt, ::onPhoneFound) }
    }
}
