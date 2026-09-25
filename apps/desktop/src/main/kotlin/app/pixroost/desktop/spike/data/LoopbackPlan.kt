package app.pixroost.desktop.spike.data

/**
 * Where the desktop receives a service's redirect: the host its registration names and a fixed port if it needs
 * one.
 */
data class LoopbackPlan(val host: String, val port: Int = 0)
