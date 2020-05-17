package nhdphuong.com.manga.data.local

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import nhdphuong.com.manga.Constants.Companion.NHENTAI_DB
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_BOOK as DOWNLOADED_BOOK
import nhdphuong.com.manga.Constants.Companion.TABLE_DOWNLOADED_IMAGE as DOWNLOADED_IMAGE
import nhdphuong.com.manga.Constants.Companion.TABLE_BOOK_TAG as BOOK_TAG
import nhdphuong.com.manga.Constants.Companion.TABLE_TAG
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.TAG_ID
import nhdphuong.com.manga.Constants.Companion.LOCAL_PATH
import nhdphuong.com.manga.Constants.Companion.ID
import nhdphuong.com.manga.Constants.Companion.TYPE
import nhdphuong.com.manga.Constants.Companion.IMAGE_TYPE
import nhdphuong.com.manga.Constants.Companion.IMAGE_WIDTH
import nhdphuong.com.manga.Constants.Companion.IMAGE_HEIGHT
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp

/*
 * Created by nhdphuong on 6/9/18.
 */
class Database {
    companion object {
        private const val TAG = "nHentai Database"
        private var mInstance: NHentaiDB? = null
        val instance: NHentaiDB
            get() {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(
                        NHentaiApp.instance.applicationContext, NHentaiDB::class.java, NHENTAI_DB
                    ).addMigrations(MIGRATE_FROM_2_TO_3)
                        .build()
                }
                return mInstance!!
            }

        private val MIGRATE_FROM_2_TO_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Logger.d(TAG, "Migrate DownLoadedImage table")
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

                Logger.d(TAG, "Migrate BookTag table")
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
