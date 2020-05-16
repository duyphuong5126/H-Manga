package nhdphuong.com.manga.usecase

import android.content.Context
import nhdphuong.com.manga.service.BookDeletingService
import javax.inject.Inject

interface StartBookDeletingUseCase {
    fun execute(bookId: String)
}

class StartBookDeletingUseCaseImpl @Inject constructor(
    private val context: Context
) : StartBookDeletingUseCase {
    override fun execute(bookId: String) {
        BookDeletingService.start(context, bookId)
    }
}
