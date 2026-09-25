package app.pixroost.desktop.spike.ui.component

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Google Photos Picker: pick photos in Google's page, the app downloads them. */
@Composable
fun PickerCard(status: String, canPick: Boolean, onPick: () -> Unit, modifier: Modifier = Modifier) {
    SectionCard("Выбор фото в Google Фото", modifier) {
        Text(status, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onPick, enabled = canPick) { Text("Выбрать фото") }
    }
}
