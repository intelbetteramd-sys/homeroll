package app.pixroost.android.spike.data

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** Hands the redirect from the browser to the screen that started the sign-in. */
object RedirectInbox {
    private val redirects = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val received: SharedFlow<Uri> = redirects.asSharedFlow()

    fun deliver(uri: Uri) {
        redirects.tryEmit(uri)
    }
}
