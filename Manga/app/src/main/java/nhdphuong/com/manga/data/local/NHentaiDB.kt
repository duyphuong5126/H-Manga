package nhdphuong.com.manga.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import nhdphuong.com.manga.data.entity.BlockedBook
import nhdphuong.com.manga.data.entity.FavoriteBook
import nhdphuong.com.manga.data.entity.RecentBook
import nhdphuong.com.manga.data.entity.book.tags.Artist
import nhdphuong.com.manga.data.entity.book.tags.Category
import nhdphuong.com.manga.data.entity.book.tags.Group
import nhdphuong.com.manga.data.entity.book.tags.Language
import nhdphuong.com.manga.data.entity.book.tags.Character
import nhdphuong.com.manga.data.entity.book.tags.Parody
import nhdphuong.com.manga.data.entity.book.tags.Tag
import nhdphuong.com.manga.data.entity.book.tags.UnknownTag
import nhdphuong.com.manga.data.local.model.BookImageModel
import nhdphuong.com.manga.data.local.model.BookTagModel
import nhdphuong.com.manga.data.local.model.DownloadedBookModel
import nhdphuong.com.manga.data.local.model.LastVisitedPage
import nhdphuong.com.manga.data.local.model.PendingDownloadBook
import nhdphuong.com.manga.data.local.model.SearchModel

/*
 * Created by nhdphuong on 6/9/18.
 */
@Database(
    entities = [
        RecentBook::class,
        FavoriteBook::class,
        BlockedBook::class,
        Tag::class,
        Artist::class,
        Character::class,
        Group::class,
        Category::class,
        Language::class,
        Parody::class,
        UnknownTag::class,
        DownloadedBookModel::class,
        BookImageModel::class,
        BookTagModel::class,
        LastVisitedPage::class,
        SearchModel::class,
        PendingDownloadBook::class
    ],
    version = 9
)
abstract class NHentaiDB : RoomDatabase() {
    abstract fun getRecentBookDAO(): BookDAO
    abstract fun getTagDAO(): TagDAO
    abstract fun getSearchDAO(): SearchDAO
}
