package app.pixroost.android.spike.ui.model

import app.pixroost.android.spike.data.FoundPc
import app.pixroost.android.spike.data.SpikeFile
import app.pixroost.android.spike.data.UploadRoute

data class TransferSpikeUiState(
    val phoneName: String,
    val addresses: List<String>,
    val pc: FoundPc? = null,
    val pinned: String? = null,
    val route: UploadRoute = UploadRoute.Direct,
    val files: List<SpikeFile> = emptyList(),
    val isPreparing: Boolean = false,
    val isRunning: Boolean = false,
    val log: List<LanEvent> = emptyList(),
)
