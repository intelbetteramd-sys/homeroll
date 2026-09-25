package app.pixroost.desktop.spike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.pixroost.desktop.spike.report.buildLanReport
import app.pixroost.desktop.spike.report.copyToClipboard
import app.pixroost.desktop.spike.ui.component.DirectPathCard
import app.pixroost.desktop.spike.ui.component.EventLogCard
import app.pixroost.desktop.spike.ui.component.FirewallCard
import app.pixroost.desktop.spike.ui.component.PcInfoCard
import app.pixroost.desktop.spike.ui.component.ReversePathCard

/** Spike S-04, the PC side: both connection paths, the firewall and the event log. */
@Composable
fun LanSpikeScreen(controller: LanSpikeController, modifier: Modifier = Modifier) {
    val state by controller.state.collectAsState()
    val log by controller.log.collectAsState()
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(UiConstants.SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(UiConstants.SECTION_SPACING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Pixroost S-04: поиск в сети",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { copyToClipboard(buildLanReport(state, log)) }) { Text("Скопировать отчёт") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.COLUMN_SPACING)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiConstants.SECTION_SPACING)) {
                PcInfoCard(state.pcName, state.addresses, state.announceStatus, state.serverStatus)
                FirewallCard(
                    firewall = state.firewall,
                    onRefresh = controller::refreshFirewall,
                    onAddRule = { controller.setRule(enabled = true) },
                    onRemoveRule = { controller.setRule(enabled = false) },
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiConstants.SECTION_SPACING)) {
                DirectPathCard(state.greetings)
                ReversePathCard(
                    state.phones,
                    onRediscover = controller::rediscover,
                    onReconnect = controller::reconnect,
                )
            }
        }
        EventLogCard(log)
    }
}
