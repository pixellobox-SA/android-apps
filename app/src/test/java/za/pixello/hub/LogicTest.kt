package za.pixello.hub

import android.telephony.TelephonyManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import za.pixello.hub.phone.networkTypeName
import za.pixello.hub.server.ENDPOINTS
import za.pixello.hub.server.Status
import za.pixello.hub.server.TAILNET
import za.pixello.hub.server.reasonFor
import za.pixello.hub.server.statusFor
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LogicTest {
    @Test fun networkNames() {
        assertEquals("5G", networkTypeName(TelephonyManager.NETWORK_TYPE_NR))
        assertEquals("4G LTE", networkTypeName(TelephonyManager.NETWORK_TYPE_LTE))
        assertEquals("Wi-Fi calling", networkTypeName(TelephonyManager.NETWORK_TYPE_IWLAN))
        assertEquals("No data", networkTypeName(TelephonyManager.NETWORK_TYPE_UNKNOWN))
    }

    @Test fun anyHttpAnswerCountsAsUp() {
        assertEquals(Status.Up(401, 12), statusFor(401, 12))
        assertEquals(Status.Up(200, 5), statusFor(200, 5))
    }

    @Test fun friendlyReasons() {
        assertTrue(reasonFor(UnknownHostException("x")).contains("Tailscale"))
        assertTrue(reasonFor(SocketTimeoutException()).contains("timed out"))
        assertTrue(reasonFor(ConnectException()).contains("refused"))
    }

    @Test fun endpointsStayOnTheTailnet() {
        ENDPOINTS.forEach { assertTrue(it.url, it.url.contains(".$TAILNET")) }
    }
}
