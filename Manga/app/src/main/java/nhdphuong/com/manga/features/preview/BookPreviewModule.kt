package nhdphuong.com.manga.features.preview

import dagger.Module
import dagger.Provides
import nhdphuong.com.manga.data.entity.book.Book

/*
 * Created by nhdphuong on 4/14/18.
 */
@Module
class BookPreviewModule(private val mView: BookPreviewContract.View, private val mBook: Book) {
    @Provides
    fun providesPreviewView(): BookPreviewContract.View = mView

    @Provides
    fun providesBook(): Book = mBook
}