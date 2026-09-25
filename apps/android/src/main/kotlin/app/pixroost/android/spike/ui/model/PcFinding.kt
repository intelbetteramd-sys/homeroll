package app.pixroost.android.spike.ui.model

import app.pixroost.android.spike.data.ConnectResult
import app.pixroost.android.spike.data.FoundPc

/** A PC found on the direct path and the result of connecting to it (null while connecting). */
data class PcFinding(val pc: FoundPc, val result: ConnectResult?)
