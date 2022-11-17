package com.testarossa.template.library.android.utils.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.IntRange

fun checkInternetConnection(context: Context): Boolean {
    return getConnectionType(context) in 1..2
}


@SuppressLint("MissingPermission")
@IntRange(from = 0, to = 3)
private fun getConnectionType(context: Context): Int {
    var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    cm.let {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    result = 2
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    result = 1
                }

                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                    result = 3
                }
            }
        }
    }
    return result
}
