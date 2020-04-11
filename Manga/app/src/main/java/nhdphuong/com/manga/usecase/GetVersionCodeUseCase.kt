package nhdphuong.com.manga.usecase

import io.reactivex.Single
import nhdphuong.com.manga.SharedPreferencesManager
import javax.inject.Inject

interface GetVersionCodeUseCase {
    fun execute(): Single<Long>
}

class GetVersionCodeUseCaseImpl @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager
) : GetVersionCodeUseCase {
    override fun execute(): Single<Long> {
        return Single.fromCallable {
            sharedPreferencesManager.currentTagVersion
        }
    }
}
