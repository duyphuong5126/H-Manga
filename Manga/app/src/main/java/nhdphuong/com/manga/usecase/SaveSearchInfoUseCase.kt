package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.data.repository.SearchRepository
import javax.inject.Inject

interface SaveSearchInfoUseCase {
    fun execute(searchInfo: String): Completable
}

class SaveSearchInfoUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository
) : SaveSearchInfoUseCase {
    override fun execute(searchInfo: String): Completable {
        return searchRepository.saveSearchEntry(searchInfo)
    }
}
