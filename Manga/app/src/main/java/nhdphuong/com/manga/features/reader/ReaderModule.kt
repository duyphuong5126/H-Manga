package nhdphuong.com.manga.features.reader

import dagger.Module
import dagger.Provides
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 5/5/18.
 */
@Module
class ReaderModule(
        private val mReaderView: ReaderContract.View,
        private val mBook: Book,
        private val mStartReadingPage: Int
) {
    @Provides
    fun providesReaderView(): ReaderContract.View = mReaderView

    @Provides
    fun providesBook(): Book = mBook

    @Provides
    fun providesStartReadingPage(): Int = mStartReadingPage
}
