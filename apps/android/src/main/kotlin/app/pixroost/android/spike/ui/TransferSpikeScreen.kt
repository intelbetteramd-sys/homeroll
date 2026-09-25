package app.pixroost.android.spike.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.pixroost.android.spike.report.buildTransferReport
import app.pixroost.android.spike.report.copyReport
import app.pixroost.android.spike.ui.component.EventLogCard
import app.pixroost.android.spike.ui.component.FilesCard
import app.pixroost.android.spike.ui.component.PcCard
import app.pixroost.android.spike.ui.component.RouteCard
import app.pixroost.android.spike.ui.component.TransferCard

/** Spike S-03, the phone side: the PC, the path, what to send, the progress and the event log. */
@Composable
fun TransferSpikeScreen(viewModel: TransferSpikeViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val transfers by viewModel.transfers.collectAsStateWithLifecycle()
    val waiting by viewModel.waitingConnections.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(UiConstants.MAX_PICKED),
        viewModel::addPicked,
    )
    val view = LocalView.current
    DisposableEffect(state.isRunning) {
        // A transfer runs while the screen is on; background work is not what this spike measures.
        view.keepScreenOn = state.isRunning
        onDispose { view.keepScreenOn = false }
    }
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
            Text("Pixroost S-03", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Button(onClick = { context.copyReport(buildTransferReport(state, transfers)) }) { Text("Отчёт") }
        }
        PcCard(state.pc, state.pinned, onForgetPin = viewModel::forgetPin)
        RouteCard(state.route, waiting, onSelect = viewModel::selectRoute)
        FilesCard(
            files = state.files,
            isPreparing = state.isPreparing,
            onCreateTestFiles = viewModel::prepareTestFiles,
            onPick = {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
            },
        )
        TransferCard(
            transfers = transfers,
            isRunning = state.isRunning,
            canStart = state.files.isNotEmpty() && !state.isPreparing,
            onStart = viewModel::start,
            onStop = viewModel::stop,
        )
        EventLogCard(state.log)
    }
}
