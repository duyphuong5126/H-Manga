package nhdphuong.com.manga.features.preview

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.Tag

/*
 * Created by nhdphuong on 4/14/18.
 */
interface BookPreviewContract {
    interface View : Base.View<Presenter> {
        fun showBookCoverImage(coverUrl: String)
        fun show1stTitle(firstTitle: String)
        fun show2ndTitle(secondTitle: String)
        fun showTagList(tagList: List<Tag>)
        fun showArtistList(artistList: List<Tag>)
        fun showLanguageList(languageList: List<Tag>)
        fun showCategoryList(categoryList: List<Tag>)
        fun showCharacterList(characterList: List<Tag>)
        fun showGroupList(groupList: List<Tag>)
        fun showParodyList(parodyList: List<Tag>)
        fun hideTagList()
        fun hideArtistList()
        fun hideLanguageList()
        fun hideCategoryList()
        fun hideCharacterList()
        fun hideGroupList()
        fun hideParodyList()
        fun showPageCount(pageCount: String)
        fun showUploadedTime(uploadedTime: String)
        fun showBookThumbnailList(thumbnailList: List<String>)
        fun showRecommendBook(bookList: List<Book>)
        fun showNoRecommendBook()
        fun showRequestStoragePermission()
        fun initDownloading(total: Int)
        fun updateDownloadProgress(progress: Int, total: Int)
        fun finishDownloading()
        fun finishDownloading(downloadFailedCount: Int, total: Int)
        fun showThisBookBeingDownloaded()
        fun showBookBeingDownloaded(bookId: String)
        fun showFavoriteBookSaved(isFavorite: Boolean)
        fun showRecentBooks(recentList: List<Int>)
        fun showFavoriteBooks(favoriteList: List<Int>)
    }

    interface Presenter : Base.Presenter {
        fun loadInfoLists()
        fun reloadCoverImage()
        fun saveCurrentAvailableCoverUrl(url: String)
        fun startReadingFrom(startReadingPage: Int)
        fun downloadBook()
        fun restartBookPreview(bookId: String)
        fun changeBookFavorite()
    }
}