package nhdphuong.com.manga.features.preview

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.tags.Tag

/*
 * Created by nhdphuong on 4/14/18.
 */
interface BookPreviewContract {
    interface View : Base.View<Presenter> {
        fun setPresenter(presenter: Presenter)
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
        fun showPageCount(pageCount: Int)
        fun showUploadedTime(uploadedTime: String)
        fun showBookThumbnailList(thumbnailList: List<String>)
        fun showRecommendBook(bookList: List<Book>)
        fun showNoRecommendBook()
        fun showRequestStoragePermission()
        fun initDownloading(total: Int)
        fun updateDownloadProgress(progress: Int, total: Int)
        fun finishDownloading()
        fun finishDownloading(downloadFailedCount: Int, total: Int)
        fun initDeleting()
        fun updateDeletingProgress(progress: Int, total: Int)
        fun finishDeleting(bookId: String)
        fun finishDeleting(bookId: String, deletingFailedCount: Int)
        fun showThisBookBeingDownloaded()
        fun showBookBeingDownloaded(bookId: String)
        fun showFavoriteBookSaved(isFavorite: Boolean)
        fun showRecentBooks(recentList: List<String>)
        fun showFavoriteBooks(favoriteList: List<String>)
        fun showOpenFolderView()
        fun startReadingFromPage(page: Int, book: Book)
        fun showUnSeenButton()
        fun hideUnSeenButton()
        fun showLastVisitedPage(page: Int, pageUrl: String)
        fun hideLastVisitedPage()
    }

    interface Presenter : Base.Presenter {
        fun enableViewDownloadedDataMode()
        fun loadInfoLists()
        fun reloadCoverImage()
        fun saveCurrentAvailableCoverUrl(url: String)
        fun startReadingFrom(startReadingPage: Int)
        fun downloadBook()
        fun deleteBook()
        fun restartBookPreview(bookId: String)
        fun changeBookFavorite()
        fun initDownloading(bookId: String, total: Int)
        fun updateDownloadingProgress(bookId: String, progress: Int, total: Int)
        fun finishDownloading(bookId: String)
        fun finishDownloading(bookId: String, downloadingFailedCount: Int, total: Int)
        fun initDeleting(bookId: String)
        fun updateDeletingProgress(bookId: String, progress: Int, total: Int)
        fun finishDeleting(bookId: String)
        fun finishDeleting(bookId: String, deletingFailedCount: Int)
        fun refreshRecentStatus()
        fun unSeenBook()
        fun loadLastVisitedPage()
    }
}
