package nhdphuong.com.manga.extension

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Build
import androidx.annotation.RequiresApi
import nhdphuong.com.manga.Logger

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.isConnectedToWifi(): Boolean {
    return hasTransportWithInternet(NetworkCapabilities.TRANSPORT_WIFI)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun Context.isConnectedToCellularNetwork(): Boolean {
    return hasTransportWithInternet(NetworkCapabilities.TRANSPORT_CELLULAR)
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun Context.hasTransportWithInternet(transportType: Int): Boolean {
    try {
        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        connectivity?.allNetworks?.takeIf { it.isNotEmpty() }?.forEach { network ->
            val networkCapabilities = connectivity.getNetworkCapabilities(network)
            val hasTransport = networkCapabilities?.hasTransport(transportType) == true
            val hasInternet = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET) == true
            if (hasTransport && hasInternet) {
                return true
            }
        }
    } catch (exception: Exception) {
        Logger.d("NetworkExts", null, exception)
    }
    return false
}

fun Context.getConnectivityManager(): ConnectivityManager? {
    return getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
}