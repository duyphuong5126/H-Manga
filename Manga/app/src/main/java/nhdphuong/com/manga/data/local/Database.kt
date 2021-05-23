package nhdphuong.com.manga.data.local

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nhdphuong.com.manga.Constants.Companion.NHENTAI_DB
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_BOOK as DOWNLOADED_BOOK
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_IMAGE as DOWNLOADED_IMAGE
import nhdphuong.com.manga.Constants.Companion.TABLE_BOOK_TAG as BOOK_TAG
import nhdphuong.com.manga.Constants.Companion.TABLE_LAST_VISITED_PAGE
import nhdphuong.com.manga.Constants.Companion.TABLE_SEARCH
import nhdphuong.com.manga.Constants.Companion.TABLE_TAG
import nhdphuong.com.manga.Constants.Companion.TABLE_PENDING_DOWNLOAD_BOOK as PENDING_DOWNLOAD_BOOK
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.CREATED_AT
import nhdphuong.com.manga.Constants.Companion.TAG_ID
import nhdphuong.com.manga.Constants.Companion.LOCAL_PATH
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.TYPE
import nhdphuong.com.manga.Constants.Companion.IMAGE_TYPE
import nhdphuong.com.manga.Constants.Companion.IMAGE_WIDTH
import nhdphuong.com.manga.Constants.Companion.IMAGE_HEIGHT
import nhdphuong.com.manga.Constants.Companion.LAST_VISITED_PAGE
import nhdphuong.com.manga.Constants.Companion.RAW_BOOK
import nhdphuong.com.manga.Constants.Companion.READING_TIMES
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.Constants.Companion.SEARCH_TIMES
import nhdphuong.com.manga.Constants.Companion.TABLE_BLOCKED_BOOK
import nhdphuong.com.manga.Constants.Companion.TABLE_RECENT_BOOK
import nhdphuong.com.manga.Constants.Companion.TITLE_PRETTY
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp

/*
 * Created by nhdphuong on 6/9/18.
 */
class Database {
    companion object {
        private val logger: Logger by lazy {
            Logger("nHentai Database")
        }

        private var mInstance: NHentaiDB? = null
        val instance: NHentaiDB
            get() {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(
                        NHentaiApp.instance.applicationContext, NHentaiDB::class.java, NHENTAI_DB
                    ).addMigrations(
                        MIGRATE_FROM_2_TO_3, MIGRATE_FROM_3_TO_4, MIGRATE_FROM_4_TO_5,
                        MIGRATE_FROM_5_TO_6, MIGRATE_FROM_6_TO_7, MIGRATE_FROM_7_TO_8,
                        MIGRATE_FROM_8_TO_9
                    ).build()
                }
                return mInstance!!
            }

        private val MIGRATE_FROM_8_TO_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS $PENDING_DOWNLOAD_BOOK ($BOOK_ID TEXT NOT NULL PRIMARY KEY, $TITLE_PRETTY TEXT NOT NULL, $RAW_BOOK TEXT NOT NULL)")
                val pendingBookIdIndex = "index_${PENDING_DOWNLOAD_BOOK}_$BOOK_ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $pendingBookIdIndex ON $PENDING_DOWNLOAD_BOOK($BOOK_ID)")
            }
        }

        private val MIGRATE_FROM_7_TO_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE $TABLE_SEARCH ADD COLUMN $SEARCH_TIMES INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE $TABLE_RECENT_BOOK ADD COLUMN $READING_TIMES INTEGER NOT NULL DEFAULT 1")
                database.execSQL("CREATE TABLE IF NOT EXISTS $TABLE_BLOCKED_BOOK ($BOOK_ID TEXT NOT NULL PRIMARY KEY)")
            }
        }

        private val MIGRATE_FROM_6_TO_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                val recent = "RecentBook"
                val newRecent = "RecentBook_new"
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS $newRecent ($BOOK_ID TEXT NOT NULL, $CREATED_AT INTEGER NOT NULL, $RAW_BOOK TEXT NOT NULL DEFAULT '', PRIMARY KEY ($BOOK_ID))"
                )

                database.execSQL("INSERT INTO $newRecent ($BOOK_ID, $CREATED_AT) SELECT $BOOK_ID, $CREATED_AT FROM $recent")

                val newFavorite = "FavoriteBook"
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS $newFavorite ($BOOK_ID TEXT NOT NULL, $CREATED_AT INTEGER NOT NULL, $RAW_BOOK TEXT NOT NULL DEFAULT '', PRIMARY KEY ($BOOK_ID))"
                )

                database.execSQL("INSERT INTO $newFavorite ($BOOK_ID, $CREATED_AT) SELECT $BOOK_ID, $CREATED_AT FROM $recent WHERE isFavorite = 1")

                database.execSQL("DROP TABLE $recent")

                database.execSQL("ALTER TABLE $newRecent RENAME TO $recent")
            }
        }

        // Foreign key constraint of tagId is dropped
        private val MIGRATE_FROM_5_TO_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create temp table
                val newBookTag = "${BOOK_TAG}_new"
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$newBookTag` (" +
                            "`$TAG_ID` INTEGER NOT NULL, " +
                            "`$BOOK_ID` TEXT NOT NULL, " +
                            "PRIMARY KEY ($BOOK_ID, $TAG_ID), " +
                            "FOREIGN KEY(`$BOOK_ID`) REFERENCES `$DOWNLOADED_BOOK`(`$ID`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                // Migrate BookTag data
                database.execSQL("INSERT INTO $newBookTag ($TAG_ID, $BOOK_ID) SELECT $TAG_ID, $BOOK_ID FROM $BOOK_TAG")

                // Drop old BookTag table
                database.execSQL("DROP TABLE $BOOK_TAG")

                // Rename temp table to BookTag
                database.execSQL("ALTER TABLE $newBookTag RENAME TO $BOOK_TAG")

                // Create indices of BookTag
                val tagIdIndex = "index_${BOOK_TAG}_$TAG_ID"
                val tagBookIdIndex = "index_${BOOK_TAG}_$BOOK_ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $tagIdIndex ON $BOOK_TAG($TAG_ID)")
                database.execSQL("CREATE INDEX IF NOT EXISTS $tagBookIdIndex ON $BOOK_TAG($BOOK_ID)")
            }
        }

        private val MIGRATE_FROM_4_TO_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$TABLE_SEARCH`(" +
                            "`$ID` INTEGER PRIMARY KEY NOT NULL, " +
                            "`$SEARCH_INFO` TEXT NOT NULL)"
                )
                val searchIdIndex = "index_${TABLE_SEARCH}_$ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $searchIdIndex ON $TABLE_SEARCH($ID)")
            }
        }

        private val MIGRATE_FROM_3_TO_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create table LastVisitedPage
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$TABLE_LAST_VISITED_PAGE`(" +
                            "`$BOOK_ID` TEXT PRIMARY KEY NOT NULL, " +
                            "`$LAST_VISITED_PAGE` INTEGER NOT NULL)"
                )

                // Create indices
                val bookIdIndex = "index_${TABLE_LAST_VISITED_PAGE}_$BOOK_ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $bookIdIndex ON $TABLE_LAST_VISITED_PAGE($BOOK_ID)")
            }
        }

        private val MIGRATE_FROM_2_TO_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                logger.d("Migrate DownLoadedImage table")
                // Create temp table
                val newImageTable = "${DOWNLOADED_IMAGE}_new"
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$newImageTable` (" +
                            "`$IMAGE_TYPE` TEXT NOT NULL, " +
                            "`$BOOK_ID` TEXT NOT NULL, " +
                            "`$LOCAL_PATH` TEXT NOT NULL, " +
                            "`$IMAGE_HEIGHT` INTEGER NOT NULL, " +
                            "`$TYPE` TEXT NOT NULL, " +
                            "`$ID` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`$IMAGE_WIDTH` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`$BOOK_ID`) REFERENCES `$DOWNLOADED_BOOK`(`$ID`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                // Migrate DownLoadedImage data
                database.execSQL(
                    "INSERT INTO $newImageTable ($ID, $BOOK_ID, $TYPE, $IMAGE_TYPE, $IMAGE_WIDTH, $IMAGE_HEIGHT, $LOCAL_PATH) " +
                            "SELECT $ID, $BOOK_ID, $TYPE, $IMAGE_TYPE, $IMAGE_WIDTH, $IMAGE_HEIGHT, $LOCAL_PATH FROM $DOWNLOADED_IMAGE"
                )

                // Drop old DownLoadedImage table
                database.execSQL("DROP TABLE $DOWNLOADED_IMAGE")

                // Rename temp table to DownLoadedImage
                database.execSQL("ALTER TABLE $newImageTable RENAME TO $DOWNLOADED_IMAGE")

                // Create indices of DownLoadedImage
                val imageBookIdIndex = "index_${DOWNLOADED_IMAGE}_$BOOK_ID"
                val imageIdIndex = "index_${DOWNLOADED_IMAGE}_$ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $imageBookIdIndex ON $DOWNLOADED_IMAGE($BOOK_ID)")
                database.execSQL("CREATE INDEX IF NOT EXISTS $imageIdIndex ON $DOWNLOADED_IMAGE($ID)")

                logger.d("Migrate BookTag table")
                // Create temp table
                val newBookTag = "${BOOK_TAG}_new"
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `$newBookTag` (" +
                            "`$TAG_ID` INTEGER NOT NULL, " +
                            "`$BOOK_ID` TEXT NOT NULL, " +
                            "PRIMARY KEY ($BOOK_ID, $TAG_ID), " +
                            "FOREIGN KEY(`$BOOK_ID`) REFERENCES `$DOWNLOADED_BOOK`(`$ID`) ON UPDATE NO ACTION ON DELETE CASCADE, " +
                            "FOREIGN KEY(`$TAG_ID`) REFERENCES `$TABLE_TAG`(`$ID`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                // Migrate BookTag data
                database.execSQL("INSERT INTO $newBookTag ($TAG_ID, $BOOK_ID) SELECT $TAG_ID, $BOOK_ID FROM $BOOK_TAG")

                // Drop old BookTag table
                database.execSQL("DROP TABLE $BOOK_TAG")

                // Rename temp table to BookTag
                database.execSQL("ALTER TABLE $newBookTag RENAME TO $BOOK_TAG")

                // Create indices of BookTag
                val tagIdIndex = "index_${BOOK_TAG}_$TAG_ID"
                val tagBookIdIndex = "index_${BOOK_TAG}_$BOOK_ID"
                database.execSQL("CREATE INDEX IF NOT EXISTS $tagIdIndex ON $BOOK_TAG($TAG_ID)")
                database.execSQL("CREATE INDEX IF NOT EXISTS $tagBookIdIndex ON $BOOK_TAG($BOOK_ID)")
            }
        }
    }
}
