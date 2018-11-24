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
        private const val ADMIN_PREFERENCE = "prefAdmin"

        private const val KEY_LAST_BOOK_LIST_REFRESH_TIME = "KEY_LAST_REFRESH_TIME"

        private const val KEY_LAST_ARTISTS_COUNT = "KEY_LAST_ARTISTS_COUNT"
        private const val KEY_LAST_CATEGORIES_COUNT = "KEY_LAST_CATEGORIES_COUNT"
        private const val KEY_LAST_CHARACTERS_COUNT = "KEY_LAST_CHARACTERS_COUNT"
        private const val KEY_LAST_LANGUAGES_COUNT = "KEY_LAST_LANGUAGES_COUNT"
        private const val KEY_LAST_PARODIES_COUNT = "KEY_LAST_PARODIES_COUNT"
        private const val KEY_LAST_GROUPS_COUNT = "KEY_LAST_GROUPS_COUNT"
        private const val KEY_LAST_TAGS_COUNT = "KEY_LAST_TAGS_COUNT"
        private const val KEY_LAST_UNKNOWN_TYPES_COUNT = "KEY_LAST_UNKNOWN_TYPES_COUNT"
        private const val KEY_TAGS_DATA_DOWNLOADED = "KEY_TAGS_DATA_DOWNLOADED"
        private const val KEY_CURRENT_TAG_VERSION = "KEY_CURRENT_TAG_VERSION"

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
    private val mAdminPreferences: SharedPreferences = NHentaiApp.instance.getSharedPreferences(ADMIN_PREFERENCE, Context.MODE_PRIVATE)

    fun setLastBookListRefreshTime(lastRefreshTime: Long) {
        mBookPreferences.edit().putLong(KEY_LAST_BOOK_LIST_REFRESH_TIME, lastRefreshTime).apply()
    }

    fun getLastBookListRefreshTime(): Long = mBookPreferences.getLong(KEY_LAST_BOOK_LIST_REFRESH_TIME, System.currentTimeMillis())

    var lastArtistsCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_ARTISTS_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_ARTISTS_COUNT, 0)

    var lastCategoriesCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_CATEGORIES_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_CATEGORIES_COUNT, 0)

    var lastCharactersCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_CHARACTERS_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_CHARACTERS_COUNT, 0)

    var lastLanguagesCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_LANGUAGES_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_LANGUAGES_COUNT, 0)

    var lastParodiesCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_PARODIES_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_PARODIES_COUNT, 0)

    var lastGroupsCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_GROUPS_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_GROUPS_COUNT, 0)

    var lastTagsCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_TAGS_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_TAGS_COUNT, 0)

    var lastUnknownTypesCount: Int
        set(value) {
            mAdminPreferences.edit().putInt(KEY_LAST_UNKNOWN_TYPES_COUNT, value).apply()
        }
        get() = mAdminPreferences.getInt(KEY_LAST_UNKNOWN_TYPES_COUNT, 0)

    var tagsDataDownloaded: Boolean
        set(value) {
            mAdminPreferences.edit().putBoolean(KEY_TAGS_DATA_DOWNLOADED, value).apply()
        }
        get() = mAdminPreferences.getBoolean(KEY_TAGS_DATA_DOWNLOADED, false)

    var currentTagVersion: Long
        set(value) {
            mAdminPreferences.edit().putLong(KEY_CURRENT_TAG_VERSION, value).apply()
        }
        get() = mAdminPreferences.getLong(KEY_CURRENT_TAG_VERSION, System.currentTimeMillis())
}