package nhdphuong.com.manga.supports

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
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

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connectivityManager.activeNetwork != null
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
}
