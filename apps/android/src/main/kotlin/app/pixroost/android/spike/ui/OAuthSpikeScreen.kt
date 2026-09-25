package app.pixroost.android.spike.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pixroost.android.spike.report.buildOAuthReport
import app.pixroost.android.spike.report.copyReport
import app.pixroost.android.spike.ui.component.EventLogCard
import app.pixroost.android.spike.ui.component.PickerCard
import app.pixroost.android.spike.ui.component.ServiceCard
import app.pixroost.android.spike.ui.model.BrowserRequest
import app.pixroost.core.spike.oauth.CloudService

/** Spike S-05, the phone side: sign-in to each cloud, the Google Photos picker and the event log. */
@Composable
fun OAuthSpikeScreen(viewModel: OAuthSpikeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pickerStatus by viewModel.picker.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.pages.collect { context.open(it) } }
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
            Text("Pixroost S-05", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { context.copyReport(buildOAuthReport(state, pickerStatus)) }) { Text("Отчёт") }
        }
        Text(state.tokenStorage, style = MaterialTheme.typography.bodyMedium)
        SelectionContainer {
            Text(state.signing.joinToString("\n"), style = MaterialTheme.typography.bodySmall)
        }
        state.services.values.forEach { service ->
            ServiceCard(
                state = service,
                onSignIn = { viewModel.signIn(service.service) },
                onRefresh = { viewModel.refresh(service.service) },
                onSignOut = { viewModel.signOut(service.service) },
            )
        }
        PickerCard(
            status = pickerStatus,
            canPick = state.services.getValue(CloudService.Google).tokenInfo != null,
            onPick = viewModel.picker::start,
        )
        EventLogCard(state.log)
    }
}

private fun Context.open(request: BrowserRequest) {
    val uri = Uri.parse(request.url)
    try {
        if (request.inCustomTab) {
            CustomTabsIntent.Builder().build().launchUrl(this, uri)
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        }
    } catch (_: ActivityNotFoundException) {
        // No browser at all: the sign-in simply does not come back, the card keeps its status.
    }
}
