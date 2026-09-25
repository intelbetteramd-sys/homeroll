package app.pixroost.desktop.spike.ui

import app.pixroost.core.spike.oauth.CloudProbe
import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.GooglePhotosPicker
import app.pixroost.core.spike.oauth.GooglePickerFlow
import app.pixroost.core.spike.oauth.OAuthClient
import app.pixroost.core.spike.oauth.OAuthConfig
import app.pixroost.core.spike.oauth.OAuthException
import app.pixroost.core.spike.oauth.OAuthSpikeConstants
import app.pixroost.core.spike.oauth.PkceFactory
import app.pixroost.core.spike.oauth.TokenSet
import app.pixroost.core.spike.oauth.codeFromRedirect
import app.pixroost.core.spike.oauth.tokenSummary
import app.pixroost.desktop.spike.data.DataConstants
import app.pixroost.desktop.spike.data.LoopbackReceiver
import app.pixroost.desktop.spike.data.TokenStore
import app.pixroost.desktop.spike.data.desktopLoopbackPlan
import app.pixroost.desktop.spike.data.desktopOAuthConfig
import app.pixroost.desktop.spike.ui.model.OAuthSpikeUiState
import app.pixroost.desktop.spike.ui.model.ServiceUiState
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom

/** State holder of the S-05 window: sign-in through the system browser, tokens encrypted with DPAPI. */
class OAuthSpikeController(private val scope: CoroutineScope) {
    private val http = HttpClient(OkHttp)
    private val oauth = OAuthClient(http, System::currentTimeMillis)
    private val probe = CloudProbe(http)
    private val random = SecureRandom()
    private val pkce = PkceFactory(
        randomBytes = { size -> ByteArray(size).also(random::nextBytes) },
        sha256 = { MessageDigest.getInstance("SHA-256").digest(it) },
    )
    private val spikeFolder = File(System.getProperty("user.home"), DataConstants.SPIKE_FOLDER)
    private val store = TokenStore(File(spikeFolder, DataConstants.TOKENS_FOLDER))
    private val holder = OAuthSpikeStateHolder(
        if (store.isEncrypted) "токены зашифрованы DPAPI" else "токены НЕ зашифрованы (не Windows)",
    )
    val state: StateFlow<OAuthSpikeUiState> = holder.state

    /** Google Photos Picker: Google's page opens in the browser, the app downloads what the user picked. */
    val picker = GooglePickerFlow(
        picker = GooglePhotosPicker(http),
        scope = scope,
        accessToken = { store.load(CloudService.Google)?.accessToken },
        openUrl = { Desktop.getDesktop().browse(URI(it + OAuthSpikeConstants.PICKER_AUTOCLOSE)) },
        log = holder::log,
        nowMillis = System::currentTimeMillis,
    )

    fun start() {
        holder.log("Токены и секрет Google хранятся в ~/${DataConstants.SPIKE_FOLDER}")
        CloudService.entries.forEach { service -> store.load(service)?.let { showTokens(service, it) } }
    }

    fun signIn(service: CloudService) = work(service, "вход через браузер…") {
        val config = config(service)
        val plan = desktopLoopbackPlan(service)
        LoopbackReceiver(plan.host, plan.port).use { receiver ->
            val codes = pkce.newPkce()
            val expectedState = pkce.newState()
            val url = oauth.authorizeUrl(config, receiver.redirectUri, expectedState, codes)
            holder.log("${service.label}: открываю браузер, адрес возврата ${receiver.redirectUri}")
            Desktop.getDesktop().browse(URI(url))
            val reply = receiver.await(OAuthSpikeConstants.SIGN_IN_TIMEOUT_MILLIS)
            val code = codeFromRedirect(expectedState, reply::get)
            val tokens = oauth.exchangeCode(config, code, receiver.redirectUri, codes)
            store.save(service, tokens)
            holder.log("${service.label}: токен получен и сохранён")
            showTokens(service, tokens)
        }
    }

    fun refresh(service: CloudService) = work(service, "обновляю токен…") {
        val tokens = store.load(service) ?: throw OAuthException("токена нет")
        val fresh = oauth.refresh(config(service), tokens)
        store.save(service, fresh)
        holder.log("${service.label}: токен обновлён")
        showTokens(service, fresh)
    }

    fun signOut(service: CloudService) {
        store.forget(service)
        holder.change(service) { ServiceUiState(service) }
        holder.log("${service.label}: токен забыт")
    }

    fun close() {
        http.close()
        scope.cancel()
    }

    private fun work(service: CloudService, status: String, block: suspend () -> Unit) {
        holder.change(service) { it.copy(status = status, isBusy = true) }
        scope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (error: IOException) {
                holder.fail(service, error)
            } catch (error: OAuthException) {
                holder.fail(service, error)
            } catch (error: TimeoutCancellationException) {
                holder.fail(service, error)
            } finally {
                holder.change(service) { it.copy(isBusy = false) }
            }
        }
    }

    /** Shows the token and proves it with one read-only request to the service. */
    private fun showTokens(service: CloudService, tokens: TokenSet) {
        holder.change(service) {
            it.copy(status = "подключено", tokenInfo = tokenSummary(tokens, System.currentTimeMillis()))
        }
        scope.launch(Dispatchers.IO) {
            try {
                val account = probe.probe(service, tokens.accessToken)
                holder.change(service) { it.copy(account = account) }
                holder.log("${service.label}: $account")
            } catch (error: IOException) {
                holder.fail(service, error)
            } catch (error: OAuthException) {
                holder.fail(service, error)
            }
        }
    }

    private fun config(service: CloudService): OAuthConfig = desktopOAuthConfig(service, spikeFolder)
        ?: throw OAuthException("нет client_id: приложение не зарегистрировано")
}
