package app.pixroost.android.spike.data

import java.net.Socket

/** A connection the PC opened to the phone, waiting to carry an upload. */
data class WaitingConnection(val socket: Socket, val acceptedAt: Long)
