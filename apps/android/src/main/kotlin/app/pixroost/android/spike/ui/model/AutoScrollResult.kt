package app.pixroost.android.spike.ui.model

import app.pixroost.android.spike.metrics.FrameStats

data class AutoScrollResult(val seconds: Float, val itemsPassed: Int, val stats: FrameStats)
