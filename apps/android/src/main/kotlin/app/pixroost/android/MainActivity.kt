package app.pixroost.android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import app.pixroost.android.spike.JankMeter
import app.pixroost.android.spike.SpikeConstants
import app.pixroost.android.spike.SpikeScreen
import app.pixroost.android.spike.spikeImageLoader

/** Spike S-02: a MediaStore grid with frame statistics instead of the placeholder screen. */
class MainActivity : ComponentActivity() {
    private var resumeCount by mutableIntStateOf(0)
    private lateinit var meter: JankMeter

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        meter = JankMeter(window)
        val imageLoader = spikeImageLoader(this)
        val refreshRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display?.refreshRate ?: SpikeConstants.DEFAULT_REFRESH_RATE
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.refreshRate
        }
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                SpikeScreen(meter, imageLoader, resumeCount, refreshRate)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        meter.isTracking = true
        resumeCount++
    }

    override fun onPause() {
        meter.isTracking = false
        super.onPause()
    }
}
