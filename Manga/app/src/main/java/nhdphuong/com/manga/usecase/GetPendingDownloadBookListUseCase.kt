package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.entity.PendingDownloadBookPreview
import nhdphuong.com.manga.data.repository.BookRepository
import javax.inject.Inject

interface GetPendingDownloadBookListUseCase {
    fun execute(limit: Int): Single<List<PendingDownloadBookPreview>>
}

class GetPendingDownloadBookListUseCaseImpl @Inject constructor(
    private val bookRepository: BookRepository
) : GetPendingDownloadBookListUseCase {
    override fun execute(limit: Int): Single<List<PendingDownloadBookPreview>> {
        return bookRepository.getPendingDownloadBooks(limit)
    }
}