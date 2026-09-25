package app.pixroost.desktop.spike.data

import java.util.concurrent.TimeUnit

/**
 * Reads the network profiles and the firewall rules for this Java executable with PowerShell, and adds or
 * removes a test rule with administrator rights (Windows asks for them). Blocking: call off the main thread.
 */
class WindowsFirewall(private val javaPath: String) {
    private val isWindows = System.getProperty("os.name").orEmpty().startsWith("Windows")

    fun state(): FirewallState = if (!isWindows) {
        FirewallState(isWindows = false, javaPath = javaPath, networkProfiles = emptyList(), rules = emptyList())
    } else {
        FirewallState(
            isWindows = true,
            javaPath = javaPath,
            networkProfiles = powershell(LanDataConstants.PROFILES_COMMAND),
            rules = powershell(rulesCommand()),
        )
    }

    /** The rule an installer would add: inbound, this program only, private networks only. */
    fun addRule() = elevated(
        "New-NetFirewallRule -DisplayName '${LanDataConstants.RULE_NAME}' -Direction Inbound " +
            "-Program '$javaPath' -Action Allow -Profile Private",
    )

    /**
     * Removes every rule for this Java executable: ours and the ones Windows creates from its prompt.
     * "Cancel" in that prompt creates a block rule, and a block rule wins over any allow rule.
     */
    fun removeRules() = elevated(
        "Get-NetFirewallApplicationFilter -Program '$javaPath' -ErrorAction SilentlyContinue | " +
            "Get-NetFirewallRule | Remove-NetFirewallRule",
    )

    private fun rulesCommand() =
        "Get-NetFirewallApplicationFilter -Program '$javaPath' -ErrorAction SilentlyContinue | Get-NetFirewallRule | " +
            "ForEach-Object { '{0} | {1} | {2} | {3} | enabled={4}' -f " +
            "\$_.DisplayName, \$_.Direction, \$_.Action, \$_.Profile, \$_.Enabled }"

    private fun elevated(command: String) {
        powershell(
            "Start-Process powershell -Verb RunAs -Wait -ArgumentList " +
                "'-NoProfile','-Command','${command.replace("'", "''")}'",
        )
    }

    private fun powershell(command: String): List<String> {
        val process = ProcessBuilder(
            "powershell",
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            LanDataConstants.UTF8_OUTPUT + command,
        )
            .redirectErrorStream(true)
            .start()
        val lines = process.inputStream.bufferedReader(Charsets.UTF_8).readLines().filter { it.isNotBlank() }
        process.waitFor(LanDataConstants.POWERSHELL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        return lines
    }
}
