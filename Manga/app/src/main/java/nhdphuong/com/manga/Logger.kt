package nhdphuong.com.manga

import android.content.pm.ApplicationInfo
import android.util.Log

class Logger(private val tag: String) {
    fun d(message: String?) {
        d(tag, "$message")
    }

    fun e(message: String?) {
        e(tag, "$message")
    }

    companion object {
        private val loggable: Boolean =
            (NHentaiApp.instance.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        fun d(tag: String?, message: String?) {
            if (loggable) {
                Log.d(tag, "$message")
            }
        }

        fun d(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.d(tag, message, throwable)
            }
        }

        fun e(tag: String?, message: String?) {
            if (loggable) {
                Log.e(tag, "$message")
            }
        }
    }
}
