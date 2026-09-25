package app.pixroost.core.spike.lan

/** Spike S-04: ports, service types and messages shared by the phone and the PC. */
object LanSpikeConstants {
    /** The PC listens here: the direct path, phone → PC. */
    const val PC_PORT = 47200

    /** The phone listens here: the reversed path, PC → phone. */
    const val PHONE_PORT = 47201

    /** The phone answers the PC's "who is there" broadcast on this UDP port. */
    const val DISCOVERY_PORT = 47202

    const val PC_SERVICE_TYPE = "_pixroost._tcp"
    const val PHONE_SERVICE_TYPE = "_pixroost-phone._tcp"

    const val HELLO = "PIXROOST-HELLO"
    const val WELCOME = "PIXROOST-WELCOME"
    const val DISCOVER = "PIXROOST-DISCOVER"
    const val PHONE_HERE = "PIXROOST-PHONE"

    const val CONNECT_TIMEOUT_MILLIS = 3000
    const val READ_TIMEOUT_MILLIS = 3000
    const val DISCOVER_INTERVAL_MILLIS = 1000L
    const val MAX_LOG_ENTRIES = 60
}
