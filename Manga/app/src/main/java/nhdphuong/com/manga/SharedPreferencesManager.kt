package nhdphuong.com.manga

import android.content.Context
import android.content.SharedPreferences
import nhdphuong.com.manga.data.entity.alternativedomain.AlternativeDomain
import nhdphuong.com.manga.views.uimodel.ReaderType
import nhdphuong.com.manga.views.uimodel.ReaderType.HorizontalPage
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
        private const val KEY_CENSORED = "KEY_CENSORED"
        private const val KEY_UPGRADE_NOTIFICATION_ALLOWED = "KEY_UPGRADE_NOTIFICATION_ALLOWED"
        private const val CURRENT_READER_MODE = "CURRENT_READER_MODE"
        private const val IS_TAP_NAVIGATION_ENABLED = "IS_TAP_NAVIGATION_ENABLED"
        private const val IS_USE_ALTERNATIVE_DOMAIN = "IS_USE_ALTERNATIVE_DOMAIN"
        private const val ALTERNATIVE_DOMAIN_ID = "ALTERNATIVE_DOMAIN_ID"
        private const val ALTERNATIVE_HOME_URL = "ALTERNATIVE_HOME_URL"
        private const val ALTERNATIVE_IMAGE_URL = "ALTERNATIVE_IMAGE_URL"
        private const val ALTERNATIVE_THUMBNAIL_URL = "ALTERNATIVE_THUMBNAIL_URL"
        private const val ALTERNATIVE_DOMAIN_RAW_DATA = "ALTERNATIVE_DOMAIN_RAW_DATA"

        private const val USER_CHECK_OUT_ALTERNATIVE_DOMAINS = "USER_CHECK_OUT_ALTERNATIVE_DOMAINS"
        private const val TIMES_OPEN_APP = "TIMES_OPEN_APP"

        private var mInstance: SharedPreferencesManager? = null
        val instance: SharedPreferencesManager
            get() {
                if (mInstance == null) {
                    mInstance = SharedPreferencesManager()
                }
                return mInstance!!
            }
    }

    private val mBookPreferences: SharedPreferences = NHentaiApp.instance.getSharedPreferences(
        BOOK_PREFERENCE,
        Context.MODE_PRIVATE
    )
    private val mAdminPreferences: SharedPreferences = NHentaiApp.instance.getSharedPreferences(
        ADMIN_PREFERENCE,
        Context.MODE_PRIVATE
    )

    fun setLastBookListRefreshTime(lastRefreshTime: Long) {
        mBookPreferences.edit().putLong(KEY_LAST_BOOK_LIST_REFRESH_TIME, lastRefreshTime).apply()
    }

    fun getLastBookListRefreshTime(): Long = mBookPreferences.getLong(
        KEY_LAST_BOOK_LIST_REFRESH_TIME,
        System.currentTimeMillis()
    )

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

    var isCensored: Boolean
        set(value) {
            mAdminPreferences.edit().putBoolean(KEY_CENSORED, value).apply()
        }
        get() = mAdminPreferences.getBoolean(KEY_CENSORED, false)

    var timesOpenApp: Int
        set(value) {
            mAdminPreferences.edit().putInt(TIMES_OPEN_APP, value).apply()
        }
        get() = mAdminPreferences.getInt(TIMES_OPEN_APP, 0)

    var checkedOutAlternativeDomains: Boolean
        set(value) {
            mAdminPreferences.edit().putBoolean(USER_CHECK_OUT_ALTERNATIVE_DOMAINS, value).apply()
        }
        get() = mAdminPreferences.getBoolean(USER_CHECK_OUT_ALTERNATIVE_DOMAINS, false)

    var isUpgradeNotificationAllowed: Boolean
        set(value) {
            mAdminPreferences.edit().putBoolean(KEY_UPGRADE_NOTIFICATION_ALLOWED, value).apply()
        }
        get() = mAdminPreferences.getBoolean(KEY_UPGRADE_NOTIFICATION_ALLOWED, true)

    val useAlternativeDomain: Boolean
        get() = mAdminPreferences.getBoolean(IS_USE_ALTERNATIVE_DOMAIN, false)

    val activeAlternativeDomainId: String
        get() = mAdminPreferences.getString(ALTERNATIVE_DOMAIN_ID, "").orEmpty()

    val alternativeHomeUrl: String
        get() = mAdminPreferences.getString(ALTERNATIVE_HOME_URL, "").orEmpty()

    val alternativeImageUrl: String
        get() = mAdminPreferences.getString(ALTERNATIVE_IMAGE_URL, "").orEmpty()

    val alternativeThumbnailUrl: String
        get() = mAdminPreferences.getString(ALTERNATIVE_THUMBNAIL_URL, "").orEmpty()

    var alternativeDomainsRawData: String
        set(value) {
            mAdminPreferences.edit().putString(ALTERNATIVE_DOMAIN_RAW_DATA, value).apply()
        }
        get() = mAdminPreferences.getString(ALTERNATIVE_DOMAIN_RAW_DATA, "").orEmpty()

    var currentReaderType: ReaderType
        set(value) {
            mBookPreferences.edit().putInt(CURRENT_READER_MODE, value.typeCode).apply()
        }
        get() {
            val typeCode = mBookPreferences.getInt(CURRENT_READER_MODE, HorizontalPage.typeCode)
            return ReaderType.fromTypeCode(typeCode)
        }

    var isTapNavigationEnabled: Boolean
        set(value) {
            mBookPreferences.edit().putBoolean(IS_TAP_NAVIGATION_ENABLED, value).apply()
        }
        get() {
            return mBookPreferences.getBoolean(IS_TAP_NAVIGATION_ENABLED, false)
        }

    fun saveActiveAlternativeDomain(alternativeDomain: AlternativeDomain) {
        mAdminPreferences.edit().putBoolean(IS_USE_ALTERNATIVE_DOMAIN, true).apply()
        mAdminPreferences.edit()
            .putString(ALTERNATIVE_DOMAIN_ID, alternativeDomain.domainId)
            .putString(ALTERNATIVE_HOME_URL, alternativeDomain.homeUrl)
            .putString(ALTERNATIVE_IMAGE_URL, alternativeDomain.imageUrl)
            .putString(ALTERNATIVE_THUMBNAIL_URL, alternativeDomain.thumbnailUrl)
            .apply()
    }

    fun clearActiveAlternativeDomain() {
        mAdminPreferences.edit().putBoolean(IS_USE_ALTERNATIVE_DOMAIN, false).apply()
        mAdminPreferences.edit()
            .putString(ALTERNATIVE_DOMAIN_ID, "")
            .putString(ALTERNATIVE_HOME_URL, "")
            .putString(ALTERNATIVE_IMAGE_URL, "")
            .putString(ALTERNATIVE_THUMBNAIL_URL, "")
            .apply()
    }

}
