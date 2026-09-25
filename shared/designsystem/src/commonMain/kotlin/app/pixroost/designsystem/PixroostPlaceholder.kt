package app.pixroost.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pixroost.core.AppInfo
import app.pixroost.core.platformName

/**
 * Temporary start screen of the project skeleton (step 1.1 of the development plan).
 * The real theme arrives in step 3.1 and the feed replaces this screen in step 3.11.
 */
@Composable
fun PixroostPlaceholder(modifier: Modifier = Modifier) {
    val dark = isSystemInDarkTheme()
    val background = if (dark) Color(0xFF0F1528) else Color(0xFFF3F5F9)
    val text = if (dark) Color(0xFFE8ECF4) else Color(0xFF172036)
    val secondary = if (dark) Color(0xFFA3ADC2) else Color(0xFF5B657A)
    val lampOutline = if (dark) Color(0xFF0F1528) else Color(0xFF172036)

    Box(modifier.fillMaxSize().background(background), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LampMark(outline = lampOutline, modifier = Modifier.size(64.dp))
            Text(AppInfo.NAME, color = text, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
            Text(platformName(), color = secondary, fontSize = 15.sp)
        }
    }
}
