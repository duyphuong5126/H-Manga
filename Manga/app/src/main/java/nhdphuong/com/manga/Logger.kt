package nhdphuong.com.manga

import android.content.pm.ApplicationInfo
import android.util.Log

class Logger {
    companion object {
        private val loggable: Boolean = (NHentaiApp.instance.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        fun d(tag: String?, message: String?) {
            if (loggable) {
                Log.d(tag, message)
            }
        }

        fun d(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.d(tag, message, throwable)
            }
        }

        fun e(tag: String?, message: String?) {
            if (loggable) {
                Log.e(tag, message)
            }
        }

        fun e(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.e(tag, message, throwable)
            }
        }

        fun i(tag: String?, message: String?) {
            if (loggable) {
                Log.i(tag, message)
            }
        }

        fun i(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.i(tag, message, throwable)
            }
        }

        fun w(tag: String?, message: String?) {
            if (loggable) {
                Log.w(tag, message)
            }
        }

        fun w(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.w(tag, message, throwable)
            }
        }

        fun wtf(tag: String?, message: String?) {
            if (loggable) {
                Log.wtf(tag, message)
            }
        }

        fun wtf(tag: String?, throwable: Throwable?) {
            if (loggable) {
                Log.wtf(tag, throwable)
            }
        }

        fun wtf(tag: String?, message: String?, throwable: Throwable?) {
            if (loggable) {
                Log.wtf(tag, message, throwable)
            }
        }
    }
}