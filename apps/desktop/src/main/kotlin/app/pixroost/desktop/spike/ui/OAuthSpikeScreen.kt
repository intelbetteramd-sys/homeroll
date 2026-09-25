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
import app.pixroost.core.spike.oauth.CloudService
import app.pixroost.desktop.spike.report.buildOAuthReport
import app.pixroost.desktop.spike.report.copyToClipboard
import app.pixroost.desktop.spike.ui.component.EventLogCard
import app.pixroost.desktop.spike.ui.component.PickerCard
import app.pixroost.desktop.spike.ui.component.ServiceCard

/** Spike S-05, the PC side: sign-in to each cloud, the Google Photos picker and the event log. */
@Composable
fun OAuthSpikeScreen(controller: OAuthSpikeController, modifier: Modifier = Modifier) {
    val state by controller.state.collectAsState()
    val pickerStatus by controller.picker.status.collectAsState()
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
                "Pixroost S-05: вход в облака",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = { copyToClipboard(buildOAuthReport(state, pickerStatus)) }) {
                Text("Скопировать отчёт")
            }
        }
        Text(state.tokenStorage, style = MaterialTheme.typography.bodyMedium)
        state.services.values.forEach { service ->
            ServiceCard(
                state = service,
                onSignIn = { controller.signIn(service.service) },
                onRefresh = { controller.refresh(service.service) },
                onSignOut = { controller.signOut(service.service) },
            )
        }
        PickerCard(
            status = pickerStatus,
            canPick = state.services.getValue(CloudService.Google).tokenInfo != null,
            onPick = controller.picker::start,
        )
        EventLogCard(state.log)
    }
}
