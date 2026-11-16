package za.pixello.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import za.pixello.hub.phone.PhoneScreen
import za.pixello.hub.server.ServerScreen
import za.pixello.hub.ui.theme.HubTheme

private enum class Tab(val label: String, val title: String, val icon: ImageVector) {
    Phone("Phone", "My phone", Icons.Filled.Phone),
    Server("Server", "Home server", Icons.Filled.Home),
    Lab("Lab", "Lab", Icons.Filled.Star),
}

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        setContent {
            var dynamicColor by remember { mutableStateOf(prefs.dynamicColor) }
            var tab by rememberSaveable { mutableStateOf(Tab.Phone) }
            var serverRefresh by remember { mutableIntStateOf(0) }
            HubTheme(dynamicColor = dynamicColor) {
                val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                Scaffold(
                    modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(tab.title) },
                            actions = {
                                if (tab == Tab.Server) {
                                    IconButton(onClick = { serverRefresh++ }) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "Check again")
                                    }
                                }
                            },
                            scrollBehavior = scroll,
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            Tab.entries.forEach { t ->
                                NavigationBarItem(
                                    selected = tab == t,
                                    onClick = { tab = t },
                                    icon = { Icon(t.icon, contentDescription = null) },
                                    label = { Text(t.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    AnimatedContent(targetState = tab, label = "tab") { current ->
                        when (current) {
                            Tab.Phone -> PhoneScreen(padding)
                            Tab.Server -> ServerScreen(padding, serverRefresh)
                            Tab.Lab -> LabScreen(padding, dynamicColor) {
                                dynamicColor = it
                                prefs.dynamicColor = it
                            }
                        }
                    }
                }
            }
        }
    }
}
