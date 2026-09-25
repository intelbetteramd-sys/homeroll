package app.pixroost.desktop.spike.data

/** Windows network profiles and the firewall rules that apply to this Java executable. */
data class FirewallState(
    val isWindows: Boolean,
    val javaPath: String,
    val networkProfiles: List<String>,
    val rules: List<String>,
)
