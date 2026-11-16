package za.pixello.hub.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/** Jacques's tailnet. Full MagicDNS names work whatever DNS search settings the phone has. */
const val TAILNET = "taild9be91.ts.net"

data class Endpoint(val name: String, val url: String, val what: String)

/** Services known from the vault (Jarvis - Complete Breakdown, 22 Oct 2026). */
val ENDPOINTS = listOf(
    Endpoint("Home Assistant", "http://homeserver.$TAILNET:8123", "Lights, plugs, speakers, sensors"),
    Endpoint("Music Assistant", "http://homeserver.$TAILNET:8095", "Whole House speakers and the LP player"),
    Endpoint("Uptime Kuma", "http://homeserver.$TAILNET:3001", "Service monitoring"),
    Endpoint("Jarvis command centre", "https://jarvis.$TAILNET", "Desktop PC (V2 removed 26 Oct, so expect down)"),
)

sealed interface Status {
    data object Checking : Status
    data class Up(val httpCode: Int, val millis: Long) : Status
    data class Down(val reason: String) : Status
}

/** Any HTTP answer, even 401 or 404, means the service is running and reachable. */
fun statusFor(httpCode: Int, millis: Long): Status = Status.Up(httpCode, millis)

fun reasonFor(e: Throwable): String = when (e) {
    is UnknownHostException -> "Name not found. Is Tailscale connected?"
    is SocketTimeoutException -> "No answer (timed out)"
    is ConnectException -> "Connection refused. Service not running?"
    is SSLException -> "Secure connection failed"
    is IOException -> e.message ?: "Network error"
    else -> e.javaClass.simpleName
}

suspend fun check(endpoint: Endpoint, timeoutMs: Int = 4_000): Status = withContext(Dispatchers.IO) {
    val started = System.nanoTime()
    try {
        val conn = (URL(endpoint.url).openConnection() as HttpURLConnection).apply {
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            instanceFollowRedirects = false
            requestMethod = "GET"
        }
        try {
            val code = conn.responseCode
            statusFor(code, (System.nanoTime() - started) / 1_000_000)
        } finally {
            conn.disconnect()
        }
    } catch (e: Exception) {
        Status.Down(reasonFor(e))
    }
}
