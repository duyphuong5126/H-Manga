package nhdphuong.com.manga.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import nhdphuong.com.manga.BuildConfig
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_APP_VERSION_CODE
import nhdphuong.com.manga.Constants.Companion.PARAM_NAME_APP_VERSION_NAME
import nhdphuong.com.manga.Logger
import javax.inject.Inject

interface AnalyticsPusher {
    fun logEvent(eventName: String)

    fun logEvent(eventName: String, param: Bundle)
}

class FirebaseAnalyticsPusherImpl @Inject constructor(context: Context) : AnalyticsPusher {

    private val firebaseAnalytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(eventName: String) {
        Logger.d(TAG, "[${BuildConfig.VERSION_NAME}] logEvent $eventName")
        firebaseAnalytics.logEvent(eventName, createOrUpdateParamBundle(null))
    }

    override fun logEvent(eventName: String, param: Bundle) {
        var paramString = ""
        param.keySet().forEach {
            paramString += "Param $it - value: ${param.getString(it)}\n"
        }
        Logger.d(TAG, "[${BuildConfig.VERSION_NAME}] logEvent $eventName\n$paramString")
        firebaseAnalytics.logEvent(eventName, createOrUpdateParamBundle(param))
    }

    private fun createOrUpdateParamBundle(param: Bundle?): Bundle {
        return (param ?: Bundle()).apply {
            putString(PARAM_NAME_APP_VERSION_NAME, BuildConfig.VERSION_NAME)
            putString(PARAM_NAME_APP_VERSION_CODE, BuildConfig.VERSION_CODE.toString())
        }
    }

    companion object {
        private const val TAG = "FirebaseAnalyticsServiceImpl"
    }
}
