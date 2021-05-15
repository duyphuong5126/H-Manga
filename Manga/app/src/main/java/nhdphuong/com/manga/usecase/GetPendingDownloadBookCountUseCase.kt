package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetPendingDownloadBookCountUseCase {
    fun execute(): Single<Int>
}

class GetPendingDownloadBookCountUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetPendingDownloadBookCountUseCase {
    override fun execute(): Single<Int> {
        return bookRepository.getPendingDownloadBookCount()
    }
}