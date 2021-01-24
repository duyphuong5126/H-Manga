package nhdphuong.com.manga.service

import android.content.Context

interface NetworkManager {

    fun attach(context: Context)

    fun detach(context: Context)

    val isConnected: Boolean

    fun addNetworkAvailableTask(task: () -> Unit)

    fun addNetworkUnAvailableTask(task: () -> Unit)
}