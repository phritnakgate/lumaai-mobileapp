package org.bkkz.lumaapp.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun Context.isConnectedToInternet() : Boolean{
    val networkManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val isConnected = networkManager.getNetworkCapabilities(networkManager.activeNetwork)?.run {
        when {
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> true
            hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> true
            else -> false
        }
    } ?: false
    return isConnected
}