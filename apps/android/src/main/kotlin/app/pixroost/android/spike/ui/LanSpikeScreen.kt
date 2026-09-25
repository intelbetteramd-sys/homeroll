package app.pixroost.android.spike.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pixroost.android.spike.report.buildLanReport
import app.pixroost.android.spike.report.copyReport
import app.pixroost.android.spike.ui.component.DirectPathCard
import app.pixroost.android.spike.ui.component.EventLogCard
import app.pixroost.android.spike.ui.component.PhoneInfoCard
import app.pixroost.android.spike.ui.component.ReversePathCard

/** Spike S-04, the phone side: finding the PC, being found by the PC, and the event log. */
@Composable
fun LanSpikeScreen(viewModel: LanSpikeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(UiConstants.SCREEN_PADDING),
        verticalArrangement = Arrangement.spacedBy(UiConstants.SECTION_SPACING),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pixroost S-04", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { context.copyReport(buildLanReport(state)) }) { Text("Отчёт") }
        }
        PhoneInfoCard(state.phoneName, state.addresses, state.serverStatus)
        DirectPathCard(state.pcs, onRediscover = viewModel::rediscover, onReconnect = viewModel::reconnect)
        ReversePathCard(state.discoverRequests, state.pcGreetings)
        EventLogCard(state.log)
    }
}
