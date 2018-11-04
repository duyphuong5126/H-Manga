package nhdphuong.com.manga.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.tags.*

/*
 * Created by nhdphuong on 6/9/18.
 */
@Database(entities = [RecentBook::class, Tag::class, Artist::class, Character::class, Group::class, Category::class,
    Language::class, Parody::class, UnknownTag::class], version = 2)
abstract class NHentaiDB : RoomDatabase() {
    abstract fun getRecentBookDAO(): RecentBookDAO
    abstract fun getTagDAO(): TagDAO
}