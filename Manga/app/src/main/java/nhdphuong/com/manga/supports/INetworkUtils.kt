package nhdphuong.com.manga.supports

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import java.net.HttpURLConnection
import java.net.URL

interface INetworkUtils {
    fun isNetworkConnected(): Boolean

    fun isReachableUrl(url: String): Boolean
}

class NetworkUtils : INetworkUtils {
    override fun isNetworkConnected(): Boolean {
        val context = NHentaiApp.instance.applicationContext

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isConnectedToWifi(context) || isConnectedToCellularNetwork(context)
        } else {
            connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    override fun isReachableUrl(url: String): Boolean {
        val urlServer = URL(url)
        val urlConn = urlServer.openConnection() as HttpURLConnection
        urlConn.connectTimeout = 3000
        urlConn.connect()
        return urlConn.responseCode == 200
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isConnectedToWifi(context: Context): Boolean {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_WIFI)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isConnectedToCellularNetwork(context: Context): Boolean {
        return hasTransport(context, NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun hasTransport(context: Context, transportType: Int): Boolean {
        try {
            val connectivity =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

            connectivity?.allNetworks?.takeIf { it.isNotEmpty() }?.forEach { network ->
                val networkCapabilities = connectivity.getNetworkCapabilities(network)
                if (networkCapabilities?.hasTransport(transportType) == true) {
                    return true
                }
            }
        } catch (exception: Exception) {
            Logger.e(TAG, "$exception")
        }
        return false
    }

    companion object {
        private const val TAG = "NetworkUtils"
    }
}
