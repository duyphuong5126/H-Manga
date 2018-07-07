package nhdphuong.com.manga

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Singleton

/*
 * Created by nhdphuong on 6/3/18.
 */
@Singleton
class SharedPreferencesManager private constructor() {
    companion object {
        private const val BOOK_PREFERENCE = "prefBook"

        private const val KEY_LAST_BOOK_LIST_REFRESH_TIME = "KEY_LAST_REFRESH_TIME"

        private var mInstance: SharedPreferencesManager? = null
        val instance: SharedPreferencesManager
            get() {
                if (mInstance == null) {
                    mInstance = SharedPreferencesManager()
                }
                return mInstance!!
            }
    }

    private val mBookPreferences: SharedPreferences = NHentaiApp.instance.getSharedPreferences(BOOK_PREFERENCE, Context.MODE_PRIVATE)

    fun setLastBookListRefreshTime(lastRefreshTime: Long) {
        mBookPreferences.edit().putLong(KEY_LAST_BOOK_LIST_REFRESH_TIME, lastRefreshTime).apply()
    }

    fun getLastBookListRefreshTime(): Long = mBookPreferences.getLong(KEY_LAST_BOOK_LIST_REFRESH_TIME, System.currentTimeMillis())
}