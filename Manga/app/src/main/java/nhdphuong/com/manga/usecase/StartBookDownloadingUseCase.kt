package nhdphuong.com.manga.usecase

import android.content.Context
import io.reactivex.Completable
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.service.BookDownloadingServices
import javax.inject.Inject

interface StartBookDownloadingUseCase {
    fun execute(book: Book): Completable
}

class StartBookDownloadingUseCaseImpl @Inject constructor(
    private val context: Context
) : StartBookDownloadingUseCase {
    override fun execute(book: Book): Completable {
        return Completable.fromCallable {
            BookDownloadingServices.start(context, book)
        }
    }
}