package za.pixello.hub

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** App-wide scope for short jobs that should outlive a single screen (the Live Update demo). */
class HubApp : Application() {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
