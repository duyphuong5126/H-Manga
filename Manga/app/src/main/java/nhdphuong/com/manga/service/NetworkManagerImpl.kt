package nhdphuong.com.manga.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import nhdphuong.com.manga.extension.getConnectivityManager
import nhdphuong.com.manga.extension.isConnectedToCellularNetwork
import nhdphuong.com.manga.extension.isConnectedToWifi
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class NetworkManagerImpl @Inject constructor() : NetworkManager {
    private var context: Context? = null
    private val isNetworkAvailable = AtomicBoolean(false)

    // Network state change from unavailable to available
    private val networkAvailableTaskList = mutableListOf<() -> Unit>()

    // Network state change from available to unavailable
    private val networkUnAvailableTaskList = mutableListOf<() -> Unit>()

    private val needToUseNetworkCallback = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val networkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val newNetworkFound = isNetworkAvailable.compareAndSet(false, true)
                if (newNetworkFound) {
                    invokeAllNetworkAvailableTasks()
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                context?.run {
                    if (!isConnectedToWifi() && !isConnectedToCellularNetwork()) {
                        isNetworkAvailable.compareAndSet(true, false)
                        invokeAllNetworkUnAvailableTasks()
                    }
                }
            }
        }

    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val noConnectivity =
                intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            if (noConnectivity) {
                isNetworkAvailable.compareAndSet(true, false)
                invokeAllNetworkUnAvailableTasks()
            } else {
                isNetworkAvailable.compareAndSet(false, true)
                invokeAllNetworkAvailableTasks()
            }
        }
    }

    override fun attach(context: Context) {
        this.context = context
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        if (needToUseNetworkCallback) {
            val networkRequest = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            context.getConnectivityManager()
                ?.registerNetworkCallback(networkRequest, networkCallback)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(
                networkReceiver,
                IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
            )
        }
    }

    override val isConnected: Boolean get() = isNetworkAvailable.get()

    override fun addNetworkAvailableTask(task: () -> Unit) {
        networkAvailableTaskList.add(task)
    }

    override fun addNetworkUnAvailableTask(task: () -> Unit) {
        networkUnAvailableTaskList.add(task)
    }

    override fun detach(context: Context) {
        this.context = null
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        if (needToUseNetworkCallback) {
            context.getConnectivityManager()?.unregisterNetworkCallback(networkCallback)
        } else {
            context.unregisterReceiver(networkReceiver)
        }
        networkAvailableTaskList.clear()
        networkUnAvailableTaskList.clear()
    }

    private fun invokeAllNetworkAvailableTasks() {
        networkAvailableTaskList.forEach {
            it.invoke()
        }
    }

    private fun invokeAllNetworkUnAvailableTasks() {
        networkUnAvailableTaskList.forEach {
            it.invoke()
        }
    }
}