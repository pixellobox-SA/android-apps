package za.pixello.hub.phone

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import za.pixello.hub.ui.InfoRow
import za.pixello.hub.ui.SectionCard

@Composable
fun PhoneScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    LifecycleResumeEffect(Unit) { refresh++; onPauseOrDispose { } }
    val snapshot by produceState<PhoneSnapshot?>(null, refresh) { value = readPhoneSnapshot(context) }
    val askPhoneState = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { refresh++ }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val s = snapshot ?: return@LazyColumn
        item {
            SectionCard("This phone") {
                InfoRow("Model", s.model)
                InfoRow("Software", s.android)
                InfoRow("Battery", "${s.batteryPercent}%" + if (s.charging) " · charging" else "")
            }
        }
        item {
            SectionCard("Connection") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = s.connection.wifi, onClick = {}, label = { Text("Wi-Fi") })
                    FilterChip(selected = s.connection.cellular, onClick = {}, label = { Text("Mobile data") })
                    FilterChip(selected = s.connection.vpn, onClick = {}, label = { Text("Tailscale") })
                }
                if (!s.connection.vpn) {
                    Text(
                        "Tailscale looks off. Server checks need it.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        val sims = s.sims
        if (sims == null) {
            item {
                SectionCard("SIM cards") {
                    Text("Allow phone access to see both SIMs, their carrier and network type.")
                    Button(onClick = { askPhoneState.launch(Manifest.permission.READ_PHONE_STATE) }) {
                        Text("Allow phone access")
                    }
                }
            }
        } else {
            items(sims, key = { it.slot }) { sim ->
                SectionCard("SIM ${sim.slot} · ${sim.name}") {
                    InfoRow("Carrier", sim.carrier.ifBlank { "Unknown" })
                    InfoRow("Network", sim.network)
                    InfoRow("Signal", sim.signalLevel?.let { "$it of 4" } ?: "Unknown")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (sim.isDefaultData) AssistChip(onClick = {}, label = { Text("Mobile data") })
                        if (sim.isDefaultVoice) AssistChip(onClick = {}, label = { Text("Calls") })
                    }
                }
            }
        }
    }
}
