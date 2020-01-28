package nhdphuong.com.manga.data.local

import androidx.room.Room
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp

/*
 * Created by nhdphuong on 6/9/18.
 */
class Database {
    companion object {
        private var mInstance: NHentaiDB? = null
        val instance: NHentaiDB
            get() {
                if (mInstance == null) {
                    mInstance = Room.databaseBuilder(
                        NHentaiApp.instance.applicationContext,
                        NHentaiDB::class.java,
                        Constants.NHENTAI_DB
                    ).fallbackToDestructiveMigration().build()
                }
                return mInstance!!
            }
    }
}
