package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.data.UploadRoute
import app.pixroost.android.spike.ui.UiConstants

/** Direct needs the firewall rule on the PC; reversed uses connections the PC opened to the phone. */
@Composable
fun RouteCard(
    route: UploadRoute,
    waitingConnections: Int,
    onSelect: (UploadRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    SectionCard("Путь", modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
            UploadRoute.entries.forEach { option ->
                FilterChip(selected = option == route, onClick = { onSelect(option) }, label = { Text(option.label) })
            }
        }
        Text(
            "Соединений от ПК ждут: $waitingConnections",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
