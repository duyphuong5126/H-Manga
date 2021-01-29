package nhdphuong.com.manga

/*
 * Created by nhdphuong on 3/17/18.
 */
class Constants {
    companion object {
        // Api constants
        const val ID = "id"
        const val MEDIA_ID = "media_id"
        const val TITLE = "title"
        const val IMAGES = "images"
        const val PAGES = "pages"
        const val IMAGE_TYPE = "t"
        const val IMAGE_WIDTH = "w"
        const val IMAGE_HEIGHT = "h"
        const val SCANLATOR = "scanlator"
        const val UPLOAD_DATE = "upload_date"
        const val TAGS_LIST = "tags"
        const val TYPE = "type"
        const val NAME = "name"
        const val URL = "url"
        const val COUNT = "count"
        const val NUM_PAGES = "num_pages"
        const val NUM_FAVORITES = "num_favorites"
        const val PER_PAGE = "per_page"
        const val RESULT = "result"
        const val COVER = "cover"
        const val THUMBNAIL = "thumbnail"
        const val TITLE_ENG = "english"
        const val TITLE_JAPANESE = "japanese"
        const val TITLE_PRETTY = "pretty"
        const val ARTIST = "artist"
        const val CHARACTER = "character"
        const val CATEGORY = "category"
        const val LANGUAGE = "language"
        const val PARODY = "parody"
        const val GROUP = "group"
        const val TAG = "tag"

        const val GALLERY_ID = "gallery_id"
        const val POSTER = "poster"
        const val USER_NAME = "username"
        const val SLUG = "slug"
        const val AVATAR_URL = "avatar_url"
        const val IS_SUPER_USER = "is_superuser"
        const val IS_STAFF = "is_staff"
        const val POST_DATE = "post_date"
        const val BODY = "body"

        // App constants
        const val RANDOM = "Random"
        const val TAGS = "Tags"
        const val CHARACTERS = "Characters"
        const val PARODIES = "Parodies"
        const val GROUPS = "Groups"
        const val INFO = "Info"
        const val ARTISTS = "Artists"
        const val DOWNLOADED = "Downloaded"

        const val SETTINGS = "Settings"
        const val ADMIN = "Admin"

        const val RECENT_TYPE = "Recent_type"
        const val RECENT = "Recent"
        const val FAVORITE = "Favorite"

        const val TAG_TYPE = "Tag_type"

        @Suppress("unused")
        const val NONE = "None"

        const val BOOK = "book"

        const val VIEW_DOWNLOADED_DATA = "viewDownloadedData"

        const val START_PAGE = "start_page"

        const val CHINESE_LANG = "cn"
        const val JAPANESE_LANG = "jp"
        const val ENGLISH_LANG = "eng"

        const val JPG = "jpg"
        const val PNG = "png"
        const val JPG_TYPE = "j"
        const val PNG_TYPE = "p"

        const val NHENTAI_DIRECTORY = "nhentai"

        const val DOWNLOAD_GREEN_LEVEL = 0.8f
        const val DOWNLOAD_YELLOW_LEVEL = 0.6f

        const val MAX_PER_PAGE = 25

        const val BOOK_PREVIEW_REQUEST = 10073
        const val DOWNLOADED_DATA_PREVIEW_REQUEST = 10074
        const val READING_REQUEST = 10075

        const val RECENT_DATA_UPDATED_ACTION = "RECENT_DATA_UPDATED"
        const val TAG_SELECTED_ACTION = "TAG_SELECTED"
        const val SELECTED_TAG = "SELECTED_TAG"
        const val REFRESH_DOWNLOADED_BOOK_LIST = "REFRESH_DOWNLOADED_BOOK_LIST"
        const val LAST_VISITED_PAGE_RESULT = "LAST_VISITED_PAGE_RESULT"
        const val DOWNLOADED_PAGES = "DOWNLOADED_PAGES"
        const val TAGS_DOWNLOADING_RESULT = "TAGS_DOWNLOADING_RESULT"

        // App DB
        const val NHENTAI_DB = "nHentai"
        const val TABLE_ARTIST = "Artists"
        const val TABLE_CHARACTER = "Characters"
        const val TABLE_CATEGORY = "Categories"
        const val TABLE_LANGUAGE = "Languages"
        const val TABLE_PARODY = "Parodies"
        const val TABLE_GROUP = "Groups"
        const val TABLE_TAG = "Tags"
        const val TABLE_UNKNOWN = "UnknownTags"
        const val TABLE_DOWNLOADED_BOOK = "DownLoadedBook"
        const val TABLE_DOWNLOADED_IMAGE = "DownLoadedImage"
        const val TABLE_BOOK_TAG = "BookTag"
        const val TABLE_LAST_VISITED_PAGE = "LastVisitedPage"
        const val TABLE_SEARCH = "Search"
        const val BOOK_ID = "bookId"
        const val TAG_ID = "tagId"
        const val CREATED_AT = "createdAt"
        const val LOCAL_PATH = "localPath"
        const val LAST_VISITED_PAGE = "lastVisitedPage"
        const val SEARCH_INFO = "searchInfo"
        const val RAW_BOOK = "rawBook"

        // Notifications
        const val NOTIFICATION_CHANNEL_ID = "nHentaiApp"
        const val NOTIFICATION_ID = 22545
        const val APP_UPGRADE_NOTIFICATION_ID = 22546

        // Broadcast receiver
        const val ACTION_DOWNLOADING_STARTED = "ACTION_DOWNLOADING_STARTED"
        const val ACTION_DOWNLOADING_PROGRESS = "ACTION_DOWNLOADING_PROGRESS"
        const val ACTION_DOWNLOADING_FAILED = "ACTION_DOWNLOADING_FAILED"
        const val ACTION_DOWNLOADING_COMPLETED = "ACTION_DOWNLOADING_COMPLETED"
        const val ACTION_DELETING_STARTED = "ACTION_DELETING_STARTED"
        const val ACTION_DELETING_PROGRESS = "ACTION_DELETING_PROGRESS"
        const val ACTION_DELETING_FAILED = "ACTION_DELETING_FAILED"
        const val ACTION_DELETING_COMPLETED = "ACTION_DELETING_COMPLETED"
        const val ACTION_SHOW_GALLERY_REFRESHING_DIALOG = "ACTION_SHOW_GALLERY_REFRESHING_DIALOG"
        const val ACTION_DISMISS_GALLERY_REFRESHING_DIALOG =
            "ACTION_DISMISS_GALLERY_REFRESHING_DIALOG"
        const val ACTION_TAGS_DOWNLOADING_PROGRESS = "ACTION_TAGS_DOWNLOADING_PROGRESS"
        const val ACTION_TAGS_DOWNLOADING_FAILED = "ACTION_TAGS_DOWNLOADING_FAILED"
        const val ACTION_TAGS_DOWNLOADING_COMPLETED = "ACTION_TAGS_DOWNLOADING_COMPLETED"

        // Analytics events
        const val EVENT_EXCEPTION = "Exception"
        const val EVENT_SEARCH = "Search"
        const val EVENT_OPEN_BOOK = "OpenBook"
        const val EVENT_TOGGLE_FAVORITE = "ToggleFavorite"

        // Analytics param name
        const val PARAM_NAME_APP_VERSION_NAME = "App_VersionName"
        const val PARAM_NAME_APP_VERSION_CODE = "App_VersionCode"
        const val PARAM_NAME_EXCEPTION_CANONICAL_CLASS_NAME = "Exception_CanonicalClassName"
        const val PARAM_NAME_EXCEPTION_CAUSE = "Exception_Cause"
        const val PARAM_NAME_EXCEPTION_MESSAGE = "Exception_Message"
        const val PARAM_NAME_EXCEPTION_LOCALIZED_MESSAGE = "Exception_LocalizedMessage"
        const val PARAM_NAME_EXCEPTION_STACK_TRACE = "Exception_StackTrace"
        const val PARAM_NAME_SEARCH_DATA = "Search_Data"
        const val PARAM_NAME_ANALYTICS_BOOK_ID = "Book_ID"
        const val PARAM_NAME_ANALYTICS_BOOK_LANGUAGE = "Book_Language"
        const val PARAM_NAME_FAVORITE_STATUS = "Favorite_Status"

        // Analytics param value
        const val PARAM_VALUE_FAVORITE = "favorite"
        const val PARAM_VALUE_UN_FAVORITE = "un_favorite"

        const val TOTAL = "total"
        const val PROGRESS = "progress"
        const val DOWNLOADING_FAILED_COUNT = "downloadingFailedCount"
        const val DELETING_FAILED_COUNT = "deletingFailedCount"

        const val TAG_PREFIXES = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        const val TAG_RESULT = "TAG_RESULT"

        const val LATEST_APP_VERSION_NUMBER = "app_version_number"
        const val LATEST_APP_VERSION_CODE = "app_version_code"
        const val IS_ACTIVATED = "is_activated"
        const val WHATS_NEW = "whats_new"
        const val DOWNLOAD_URL = "download_url"

        const val ALTERNATIVE_DOMAINS_DATA_VERSION = "version"
        const val ALTERNATIVE_DOMAINS = "supported_domains"
        const val ALTERNATIVE_DOMAIN_ID = "domain_id"
        const val ALTERNATIVE_HOME_URL = "home_url"
        const val ALTERNATIVE_IMAGE_URL = "i_url"
        const val ALTERNATIVE_THUMBNAIL_URL = "t_url"
    }
}
