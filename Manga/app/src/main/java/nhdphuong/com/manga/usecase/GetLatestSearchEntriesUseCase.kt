package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.SearchRepository
import javax.inject.Inject

interface GetLatestSearchEntriesUseCase {
    fun execute(maximumEntries: Int): Single<List<String>>
}

class GetLatestSearchEntriesUseCaseImpl @Inject constructor(
    private val searchRepository: SearchRepository
) : GetLatestSearchEntriesUseCase {
    override fun execute(maximumEntries: Int): Single<List<String>> {
        return searchRepository.getLatestSearchEntries(maximumEntries).map(List<String>::distinct)
    }
}
