package app.pixroost.desktop.spike.data

import app.pixroost.core.spike.oauth.CloudService

/**
 * Where the browser returns on the PC. Yandex and Dropbox compare the whole redirect URI, port included, so they
 * share one port registered in advance. Microsoft ignores the port of `http://localhost`, Google accepts any port
 * of `http://127.0.0.1`: both get a free one.
 */
fun desktopLoopbackPlan(service: CloudService): LoopbackPlan = when (service) {
    CloudService.Yandex, CloudService.Dropbox -> LoopbackPlan("127.0.0.1", DataConstants.FIXED_LOOPBACK_PORT)
    CloudService.Microsoft -> LoopbackPlan("localhost")
    CloudService.Google -> LoopbackPlan("127.0.0.1")
}
