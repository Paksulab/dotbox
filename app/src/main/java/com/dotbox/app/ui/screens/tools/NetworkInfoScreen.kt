package com.dotbox.app.ui.screens.tools

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dotbox.app.ui.components.ToolScreenScaffold
import com.dotbox.app.ui.theme.JetBrainsMono
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL

private data class NetInfo(
    val isConnected: Boolean = false,
    val type: String = "Unknown",
    val wifiSsid: String = "N/A",
    val localIp: String = "N/A",
    val publicIp: String = "Loading...",
    val linkSpeed: String = "N/A",
    val signalStrength: String = "N/A",
    val dns: String = "N/A",
    val isVpn: Boolean = false,
    val isMetered: Boolean = false,
)

@Composable
fun NetworkInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var info by remember { mutableStateOf(NetInfo()) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        info = gatherNetInfo(context)
    }

    ToolScreenScaffold(
        title = "Network Info",
        onBack = onBack,
        actions = {
            IconButton(onClick = { refreshKey++ }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Connection status badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (info.isConnected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    )
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (info.isConnected) "● Connected" else "● Disconnected",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (info.isConnected) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.error,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InfoSection("Connection") {
                NetRow("Type", info.type)
                NetRow("Metered", if (info.isMetered) "Yes" else "No")
                NetRow("VPN", if (info.isVpn) "Active" else "No")
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoSection("WiFi") {
                NetRow("SSID", info.wifiSsid)
                NetRow("Link Speed", info.linkSpeed)
                NetRow("Signal", info.signalStrength)
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoSection("IP Addresses") {
                NetRow("Local IP", info.localIp)
                NetRow("Public IP", info.publicIp)
                NetRow("DNS", info.dns)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun NetRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Suppress("DEPRECATION")
private suspend fun gatherNetInfo(context: Context): NetInfo {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork
    val caps = network?.let { cm.getNetworkCapabilities(it) }

    val isConnected = caps != null &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)

    val type = when {
        caps == null -> "None"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
        caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
        else -> "Other"
    }

    val isVpn = caps?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true
    val isMetered = cm.isActiveNetworkMetered

    // WiFi info
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val wifiInfo = wifiManager.connectionInfo
    val ssid = wifiInfo?.ssid?.replace("\"", "") ?: "N/A"
    val linkSpeed = if (wifiInfo?.linkSpeed != null && wifiInfo.linkSpeed > 0) "${wifiInfo.linkSpeed} Mbps" else "N/A"
    val rssi = wifiInfo?.rssi
    val signalLevel = if (rssi != null) {
        val level = WifiManager.calculateSignalLevel(rssi, 5)
        "$level/4 ($rssi dBm)"
    } else "N/A"

    // Local IP
    val localIp = try {
        NetworkInterface.getNetworkInterfaces()?.toList()
            ?.flatMap { it.inetAddresses.toList() }
            ?.firstOrNull { !it.isLoopbackAddress && it is Inet4Address }
            ?.hostAddress ?: "N/A"
    } catch (_: Exception) { "N/A" }

    // DNS
    val dns = try {
        val linkProps = cm.getLinkProperties(network)
        linkProps?.dnsServers?.joinToString(", ") { it.hostAddress ?: "" } ?: "N/A"
    } catch (_: Exception) { "N/A" }

    // Public IP (async)
    val publicIp = if (isConnected) {
        try {
            withContext(Dispatchers.IO) {
                URL("https://api.ipify.org").readText().trim()
            }
        } catch (_: Exception) { "Unavailable" }
    } else "N/A"

    return NetInfo(
        isConnected = isConnected,
        type = type,
        wifiSsid = ssid,
        localIp = localIp,
        publicIp = publicIp,
        linkSpeed = linkSpeed,
        signalStrength = signalLevel,
        dns = dns,
        isVpn = isVpn,
        isMetered = isMetered,
    )
}
