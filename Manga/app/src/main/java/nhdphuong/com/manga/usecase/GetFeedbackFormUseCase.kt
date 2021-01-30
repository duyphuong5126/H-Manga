package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.data.repository.MasterDataRepository
import javax.inject.Inject

interface GetFeedbackFormUseCase {
    fun execute(): Single<String>
}

class GetFeedbackFormUseCaseImpl @Inject constructor(
    private val masterDataRepository: MasterDataRepository
) : GetFeedbackFormUseCase {
    override fun execute(): Single<String> {
        return masterDataRepository.fetchFeedbackFormUrl()
    }
}