package nhdphuong.com.manga.usecase

import io.reactivex.Completable
import nhdphuong.com.manga.data.repository.SearchRepository
import javax.inject.Inject

interface DeleteSearchSuggestionUseCase {
    fun execute(searchInfo: String): Completable
}

class DeleteSearchSuggestionUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository
) : DeleteSearchSuggestionUseCase {
    override fun execute(searchInfo: String): Completable {
        return searchRepository.deleteSearchInfo(searchInfo)
    }
}