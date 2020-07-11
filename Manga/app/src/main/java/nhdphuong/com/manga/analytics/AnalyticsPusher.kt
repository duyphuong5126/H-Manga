package nhdphuong.com.manga.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import nhdphuong.com.manga.Logger
import javax.inject.Inject

interface AnalyticsPusher {
    fun logEvent(eventName: String)

    fun logEvent(eventName: String, param: Bundle)
}

class FirebaseAnalyticsPusherImpl @Inject constructor(context: Context) : AnalyticsPusher {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(eventName: String) {
        Logger.d(TAG, "logEvent $eventName")
        firebaseAnalytics.logEvent(eventName, null)
    }

    override fun logEvent(eventName: String, param: Bundle) {
        var paramString = ""
        param.keySet().forEach {
            paramString += "Param $it - value: ${param.getString(it)}\n"
        }
        Logger.d(TAG, "logEvent $eventName - params: $paramString")
        firebaseAnalytics.logEvent(eventName, param)
    }

    companion object {
        private const val TAG = "FirebaseAnalyticsServiceImpl"
    }
}
