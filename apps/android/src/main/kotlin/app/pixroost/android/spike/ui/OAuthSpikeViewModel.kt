package app.pixroost.android.spike.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.pixroost.android.spike.data.KeystoreTokenStore
import app.pixroost.android.spike.data.RedirectInbox
import app.pixroost.android.spike.data.signingFingerprints
import app.pixroost.android.spike.ui.model.BrowserRequest
import app.pixroost.android.spike.ui.model.OAuthSpikeUiState
import app.pixroost.android.spike.ui.model.PendingSignIn
import app.pixroost.android.spike.ui.model.ServiceUiState
import app.pixroost.core.spike.oauth.CloudProbe
import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.core.spike.oauth.GooglePhotosPicker
import app.pixroost.core.spike.oauth.GooglePickerFlow
import app.pixroost.core.spike.oauth.OAuthClient
import app.pixroost.core.spike.oauth.OAuthConfig
import app.pixroost.core.spike.oauth.OAuthException
import app.pixroost.core.spike.oauth.PkceFactory
import app.pixroost.core.spike.oauth.TokenSet
import app.pixroost.core.spike.oauth.androidRedirectUri
import app.pixroost.core.spike.oauth.codeFromRedirect
import app.pixroost.core.spike.oauth.oauthConfig
import app.pixroost.core.spike.oauth.tokenSummary
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

/** Sign-in in Custom Tabs, redirect back into the app, tokens encrypted with a Keystore key. */
class OAuthSpikeViewModel(application: Application) : AndroidViewModel(application) {
    private val http = HttpClient(OkHttp)
    private val oauth = OAuthClient(http, System::currentTimeMillis)
    private val probe = CloudProbe(http)
    private val random = SecureRandom()
    private val pkce = PkceFactory(
        randomBytes = { size -> ByteArray(size).also(random::nextBytes) },
        sha256 = { MessageDigest.getInstance("SHA-256").digest(it) },
    )
    private val store = KeystoreTokenStore(application)
    private val holder = OAuthSpikeStateHolder(
        tokenStorage = "токены зашифрованы ключом Android Keystore",
        signing = application.signingFingerprints(),
    )
    private var pending: PendingSignIn? = null
    val state: StateFlow<OAuthSpikeUiState> = holder.state

    /** Pages for the screen to open: sign-in in Custom Tabs, the picker through a VIEW intent. */
    private val _pages = MutableSharedFlow<BrowserRequest>(extraBufferCapacity = 1)
    val pages: SharedFlow<BrowserRequest> = _pages.asSharedFlow()

    val picker = GooglePickerFlow(
        picker = GooglePhotosPicker(http),
        scope = viewModelScope,
        accessToken = { store.load(CloudService.Google)?.accessToken },
        openUrl = { _pages.tryEmit(BrowserRequest(it, inCustomTab = false)) },
        log = holder::log,
        nowMillis = System::currentTimeMillis,
    )

    init {
        CloudService.entries.forEach { service -> store.load(service)?.let { showTokens(service, it) } }
        viewModelScope.launch { RedirectInbox.received.collect(::onRedirect) }
    }

    fun signIn(service: CloudService) {
        val config = oauthConfig(service, isDesktop = false)
        if (config == null) {
            holder.change(service) { it.copy(status = "нет client_id: приложение не зарегистрировано") }
            return
        }
        val codes = pkce.newPkce()
        val signIn = PendingSignIn(service, pkce.newState(), codes, androidRedirectUri(service))
        pending = signIn
        holder.change(service) { it.copy(status = "вход в браузере…") }
        holder.log("${service.label}: открываю вход, адрес возврата ${signIn.redirectUri}")
        _pages.tryEmit(BrowserRequest(oauth.authorizeUrl(config, signIn.redirectUri, signIn.state, codes), true))
    }

    fun refresh(service: CloudService) = work(service) {
        val fresh = oauth.refresh(config(service), store.load(service) ?: throw OAuthException("токена нет"))
        store.save(service, fresh)
        holder.log("${service.label}: токен обновлён")
        showTokens(service, fresh)
    }

    fun signOut(service: CloudService) {
        store.forget(service)
        holder.change(service) { ServiceUiState(service) }
        holder.log("${service.label}: токен забыт")
    }

    override fun onCleared() = http.close()

    private fun onRedirect(uri: Uri) {
        val signIn = pending ?: return holder.log("возврат без начатого входа: ${uri.scheme}")
        pending = null
        holder.log("${signIn.service.label}: браузер вернул ${uri.scheme}:…")
        work(signIn.service) {
            val code = codeFromRedirect(signIn.state, uri::getQueryParameter)
            val tokens = oauth.exchangeCode(config(signIn.service), code, signIn.redirectUri, signIn.pkce)
            store.save(signIn.service, tokens)
            holder.log("${signIn.service.label}: токен получен и сохранён")
            showTokens(signIn.service, tokens)
        }
    }

    private fun work(service: CloudService, block: suspend () -> Unit) {
        holder.change(service) { it.copy(isBusy = true) }
        viewModelScope.launch {
            try {
                block()
            } catch (error: IOException) {
                holder.fail(service, error)
            } catch (error: OAuthException) {
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
        work(service) {
            val account = probe.probe(service, tokens.accessToken)
            holder.change(service) { it.copy(account = account) }
            holder.log("${service.label}: $account")
        }
    }

    private fun config(service: CloudService): OAuthConfig =
        oauthConfig(service, isDesktop = false) ?: throw OAuthException("нет client_id")
}
