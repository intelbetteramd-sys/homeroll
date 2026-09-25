package app.pixroost.desktop.spike.ui.model

import app.pixroost.desktop.spike.data.ConnectResult
import app.pixroost.desktop.spike.data.FoundPhone

/** A phone found on the reversed path and the result of connecting to it (null while connecting). */
data class PhoneFinding(val phone: FoundPhone, val result: ConnectResult?)
