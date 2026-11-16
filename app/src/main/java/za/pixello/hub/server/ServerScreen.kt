package za.pixello.hub.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import za.pixello.hub.ui.SectionCard

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ServerScreen(contentPadding: PaddingValues, refreshKey: Int) {
    val results = remember { mutableStateMapOf<String, Status>() }
    LaunchedEffect(refreshKey) {
        ENDPOINTS.forEach { results[it.name] = Status.Checking }
        coroutineScope { ENDPOINTS.forEach { e -> launch { results[e.name] = check(e) } } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp, end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 16.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                "Checked over Tailscale ($TAILNET). Nothing here is reachable from the internet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(ENDPOINTS, key = { it.name }) { endpoint ->
            val status = results[endpoint.name] ?: Status.Checking
            SectionCard(endpoint.name) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (status) {
                        Status.Checking -> LoadingIndicator(Modifier.size(32.dp))
                        is Status.Up -> Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        is Status.Down -> Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.error)
                    }
                    Column {
                        Text(
                            when (status) {
                                Status.Checking -> "Checking…"
                                is Status.Up -> "Up · ${status.millis} ms"
                                is Status.Down -> "Down · ${status.reason}"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(endpoint.what, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
