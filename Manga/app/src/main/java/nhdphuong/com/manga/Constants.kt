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

        // App constants
        const val RANDOM = "Random"
        const val TAGS = "Tags"
        const val CHARACTERS = "Characters"
        const val PARODIES = "Parodies"
        const val GROUPS = "Groups"
        const val INFO = "Info"
        const val ARTISTS = "Artists"
        const val DOWNLOADED = "Downloaded"

        @Suppress("unused")
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

        @Suppress("unused")
        const val MAX_PER_PAGE = 25

        const val BOOK_PREVIEW_RESULT = 10073

        const val RECENT_DATA_UPDATED_ACTION = "RECENT_DATA_UPDATED"
        const val TAG_SELECTED_ACTION = "TAG_SELECTED"
        const val SELECTED_TAG = "SELECTED_TAG"

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
        const val BOOK_ID = "bookId"
        const val TAG_ID = "tagId"
        const val IS_FAVORITE = "isFavorite"
        const val CREATED_AT = "createdAt"
        const val LOCAL_PATH = "localPath"

        // Notifications
        const val NOTIFICATION_CHANNEL_ID = "nHentaiApp"
        const val NOTIFICATION_ID = 22545

        // Broadcast receiver
        const val ACTION_DOWNLOADING_STARTED = "ACTION_DOWNLOADING_STARTED"
        const val ACTION_DOWNLOADING_PROGRESS = "ACTION_DOWNLOADING_PROGRESS"
        const val ACTION_DOWNLOADING_FAILED = "ACTION_DOWNLOADING_FAILED"
        const val ACTION_DOWNLOADING_COMPLETED = "ACTION_DOWNLOADING_COMPLETED"

        const val TOTAL = "total"
        const val PROGRESS = "progress"
        const val DOWNLOADING_FAILED_COUNT = "downloadingFailedCount"

        const val TAG_PREFIXES = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        const val TAG_RESULT = "TAG_RESULT"
    }
}
