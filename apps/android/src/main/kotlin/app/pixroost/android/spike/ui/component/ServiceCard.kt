package app.pixroost.android.spike.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.ui.UiConstants
import app.pixroost.android.spike.ui.model.ServiceUiState

/** One cloud: sign in through the browser, refresh the token, sign out. */
@Composable
fun ServiceCard(
    state: ServiceUiState,
    onSignIn: () -> Unit,
    onRefresh: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard(state.service.label, modifier) {
        Text(state.status, style = style)
        state.account?.let { Text(it, style = style) }
        state.tokenInfo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        Row(horizontalArrangement = Arrangement.spacedBy(UiConstants.ROW_SPACING)) {
            Button(onClick = onSignIn, enabled = !state.isBusy) { Text("Войти") }
            OutlinedButton(onClick = onRefresh, enabled = !state.isBusy && state.tokenInfo != null) {
                Text("Обновить токен")
            }
            OutlinedButton(onClick = onSignOut, enabled = !state.isBusy && state.tokenInfo != null) { Text("Выйти") }
        }
    }
}
