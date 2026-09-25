package app.pixroost.desktop.spike.data

object DataConstants {
    const val CALLBACK_PATH = "/callback"
    const val HTTP_OK = 200
    const val DONE_PAGE =
        "<!doctype html><meta charset=utf-8><title>Pixroost</title>" +
            "<body style='font-family:sans-serif;padding:2em'><h2>Вход выполнен</h2><p>Вкладку можно закрыть.</p>"
    const val SPIKE_FOLDER = ".pixroost-spike"
    const val TOKENS_FOLDER = "tokens"

    /** Registered in advance for Yandex and Dropbox, which compare the redirect's port too. */
    const val FIXED_LOOPBACK_PORT = 47310

    /** Google's desktop client secret: in this file next to the tokens, never in the repository. */
    const val GOOGLE_SECRET_FILE = "google-client-secret.txt"
}
