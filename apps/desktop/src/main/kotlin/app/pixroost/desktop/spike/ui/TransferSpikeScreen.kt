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
import app.pixroost.desktop.spike.report.buildTransferReport
import app.pixroost.desktop.spike.report.copyToClipboard
import app.pixroost.desktop.spike.ui.component.EventLogCard
import app.pixroost.desktop.spike.ui.component.FirewallCard
import app.pixroost.desktop.spike.ui.component.PhonesCard
import app.pixroost.desktop.spike.ui.component.ServerCard
import app.pixroost.desktop.spike.ui.component.UploadsCard

/** Spike S-03, the PC side: the HTTPS server, the firewall, the reversed path and the received files. */
@Composable
fun TransferSpikeScreen(controller: TransferSpikeController, modifier: Modifier = Modifier) {
    val state by controller.state.collectAsState()
    val uploads by controller.uploads.collectAsState()
    val links by controller.linkStats.collectAsState()
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
                "Pixroost S-03: передача на ПК",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { copyToClipboard(buildTransferReport(state, uploads.values, links, log)) }) {
                Text("Скопировать отчёт")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.COLUMN_SPACING)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(UiConstants.SECTION_SPACING)) {
                ServerCard(state)
                PhonesCard(state.phones, links, onRediscover = controller::rediscover)
            }
            FirewallCard(
                firewall = state.firewall,
                onRefresh = controller::refreshFirewall,
                onAddRule = { controller.setRule(enabled = true) },
                onRemoveRule = { controller.setRule(enabled = false) },
                modifier = Modifier.weight(1f),
            )
        }
        UploadsCard(uploads.values)
        EventLogCard(log)
    }
}
