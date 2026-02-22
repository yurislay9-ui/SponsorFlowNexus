/*
 * SponsorFlow Nexus v2.3 - Network Monitor (WiFi/4G Handover)
 */
package com.sponsorflow.nexus.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

sealed class NetworkState {
    object Available : NetworkState()
    object Lost : NetworkState()
    data class Changed(val type: String) : NetworkState()
}

class NetworkMonitor(private val context: Context) {
    
    private var connectivityManager: ConnectivityManager? = null
    private var callback: NetworkCallback? = null
    private var listener: ((NetworkState) -> Unit)? = null
    private var isWifi = false
    private var isCellular = false
    
    fun init(onStateChange: (NetworkState) -> Unit) {
        listener = onStateChange
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) 
            as ConnectivityManager
        
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        callback = NetworkCallback()
        connectivityManager?.registerNetworkCallback(request, callback!!)
    }
    
    fun isOnline(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val caps = connectivityManager?.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    fun getConnectionType(): String {
        val network = connectivityManager?.activeNetwork ?: return "none"
        val caps = connectivityManager?.getNetworkCapabilities(network) ?: return "none"
        
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "unknown"
        }
    }
    
    fun unregister() {
        callback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        callback = null
    }
    
    private inner class NetworkCallback : ConnectivityManager.NetworkCallback() {
        
        override fun onAvailable(network: Network) {
            val type = getConnectionType()
            isWifi = type == "wifi"
            isCellular = type == "cellular"
            listener?.invoke(NetworkState.Available)
        }
        
        override fun onLost(network: Network) {
            listener?.invoke(NetworkState.Lost)
        }
        
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            val wasWifi = isWifi
            val wasCellular = isCellular
            
            isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            
            // Detectar cambio de red
            if (wasWifi && isCellular) {
                listener?.invoke(NetworkState.Changed("wifi_to_cellular"))
            } else if (wasCellular && isWifi) {
                listener?.invoke(NetworkState.Changed("cellular_to_wifi"))
            }
        }
    }
}