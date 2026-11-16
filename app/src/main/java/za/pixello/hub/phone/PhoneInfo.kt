package za.pixello.hub.phone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager

data class SimInfo(
    val slot: Int,
    val name: String,
    val carrier: String,
    val network: String,
    /** 0 (none) to 4 (full), or null when unknown. */
    val signalLevel: Int?,
    val isDefaultData: Boolean,
    val isDefaultVoice: Boolean,
)

data class ConnectionInfo(
    val wifi: Boolean,
    val cellular: Boolean,
    /** True when a VPN is up. On this phone that is Tailscale. */
    val vpn: Boolean,
)

data class PhoneSnapshot(
    val model: String,
    val android: String,
    val batteryPercent: Int,
    val charging: Boolean,
    val connection: ConnectionInfo,
    /** Null when the phone-state permission hasn't been granted. */
    val sims: List<SimInfo>?,
)

/** Plain-language name for a TelephonyManager network type. */
fun networkTypeName(type: Int): String = when (type) {
    TelephonyManager.NETWORK_TYPE_NR -> "5G"
    TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
    TelephonyManager.NETWORK_TYPE_IWLAN -> "Wi-Fi calling"
    TelephonyManager.NETWORK_TYPE_HSPAP,
    TelephonyManager.NETWORK_TYPE_HSPA,
    TelephonyManager.NETWORK_TYPE_HSDPA,
    TelephonyManager.NETWORK_TYPE_HSUPA,
    TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
    TelephonyManager.NETWORK_TYPE_EDGE,
    TelephonyManager.NETWORK_TYPE_GPRS,
    TelephonyManager.NETWORK_TYPE_GSM -> "2G"
    TelephonyManager.NETWORK_TYPE_UNKNOWN -> "No data"
    else -> "Other"
}

fun readPhoneSnapshot(context: Context): PhoneSnapshot {
    val battery = context.getSystemService(BatteryManager::class.java)
    return PhoneSnapshot(
        model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
        android = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        batteryPercent = battery.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
        charging = battery.isCharging,
        connection = readConnection(context),
        sims = if (hasPhoneStatePermission(context)) readSims(context) else null,
    )
}

fun hasPhoneStatePermission(context: Context) =
    context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

@Suppress("DEPRECATION") // allNetworks is the simplest way to see every transport at once.
private fun readConnection(context: Context): ConnectionInfo {
    val cm = context.getSystemService(ConnectivityManager::class.java)
    val caps = cm.allNetworks.mapNotNull { cm.getNetworkCapabilities(it) }
    fun any(transport: Int) = caps.any { it.hasTransport(transport) }
    return ConnectionInfo(
        wifi = any(NetworkCapabilities.TRANSPORT_WIFI),
        cellular = any(NetworkCapabilities.TRANSPORT_CELLULAR),
        vpn = any(NetworkCapabilities.TRANSPORT_VPN),
    )
}

@SuppressLint("MissingPermission") // Only called after hasPhoneStatePermission().
private fun readSims(context: Context): List<SimInfo> {
    val sm = context.getSystemService(SubscriptionManager::class.java)
    val baseTm = context.getSystemService(TelephonyManager::class.java)
    val defaultData = SubscriptionManager.getDefaultDataSubscriptionId()
    val defaultVoice = SubscriptionManager.getDefaultVoiceSubscriptionId()
    return sm.activeSubscriptionInfoList.orEmpty()
        .sortedBy { it.simSlotIndex }
        .map { sub ->
            val tm = baseTm.createForSubscriptionId(sub.subscriptionId)
            SimInfo(
                slot = sub.simSlotIndex + 1,
                name = sub.displayName?.toString().orEmpty().ifBlank { "SIM ${sub.simSlotIndex + 1}" },
                carrier = tm.networkOperatorName.ifBlank { sub.carrierName?.toString().orEmpty() },
                network = networkTypeName(tm.dataNetworkType),
                signalLevel = tm.signalStrength?.level,
                isDefaultData = sub.subscriptionId == defaultData,
                isDefaultVoice = sub.subscriptionId == defaultVoice,
            )
        }
}
