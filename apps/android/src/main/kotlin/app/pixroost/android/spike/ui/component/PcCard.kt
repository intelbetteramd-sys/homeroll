package app.pixroost.android.spike.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.pixroost.android.spike.data.FoundPc
import app.pixroost.android.spike.util.formatFingerprint

/** The PC found by its broadcast and the certificate the phone trusts; compare it with the PC window. */
@Composable
fun PcCard(pc: FoundPc?, pinned: String?, onForgetPin: () -> Unit, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.bodyMedium
    SectionCard("ПК", modifier) {
        Text(pc?.let { "${it.name}, ${it.host}" } ?: "ждём рассылку от ПК…", style = style)
        Text("Сертификат: ${pinned?.let(::formatFingerprint) ?: "запомнится при первом подключении"}", style = style)
        if (pinned != null) OutlinedButton(onClick = onForgetPin) { Text("Забыть сертификат") }
    }
}
