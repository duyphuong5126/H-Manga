package nhdphuong.com.manga.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import nhdphuong.com.manga.data.entity.RecentBook

/*
 * Created by nhdphuong on 6/9/18.
 */
@Database(entities = [(RecentBook::class)], version = 1)
abstract class NHentaiDB : RoomDatabase() {
    abstract fun getRecentBookDAO(): RecentBookDAO
}