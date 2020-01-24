package nhdphuong.com.manga.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag

/*
 * Created by nhdphuong on 6/9/18.
 */
@Database(
    entities = [
        RecentBook::class,
        Tag::class,
        Artist::class,
        Character::class,
        Group::class,
        Category::class,
        Language::class,
        Parody::class,
        UnknownTag::class],
    version = 2
)
abstract class NHentaiDB : RoomDatabase() {
    abstract fun getRecentBookDAO(): RecentBookDAO
    abstract fun getTagDAO(): TagDAO
}
