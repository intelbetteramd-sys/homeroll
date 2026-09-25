package app.pixroost.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import app.pixroost.android.spike.ui.OAuthSpikeScreen
import app.pixroost.android.spike.ui.OAuthSpikeViewModel

/** Spike S-05: the cloud sign-in screen instead of the placeholder screen. */
class MainActivity : ComponentActivity() {
    private val viewModel: OAuthSpikeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                OAuthSpikeScreen(viewModel)
            }
        }
    }
}
