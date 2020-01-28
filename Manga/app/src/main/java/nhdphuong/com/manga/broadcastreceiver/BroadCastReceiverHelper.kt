package nhdphuong.com.manga.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

object BroadCastReceiverHelper {
    fun sendBroadCast(fromContext: Context?, action: String, data: Bundle? = null) {
        fromContext?.run {
            val intent = Intent(action).apply {
                data?.let { bundleData ->
                    putExtras(bundleData)
                }
            }
            sendBroadcast(intent)
        }
    }

    fun registerBroadcastReceiver(
        context: Context?,
        broadcastReceiver: BroadcastReceiver,
        vararg actions: String
    ) {
        context?.registerReceiver(broadcastReceiver, IntentFilter().apply {
            actions.forEach { action -> addAction(action) }
        })
    }

    fun unRegisterBroadcastReceiver(context: Context?, broadcastReceiver: BroadcastReceiver) {
        context?.unregisterReceiver(broadcastReceiver)
    }
}