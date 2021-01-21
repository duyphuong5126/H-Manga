package nhdphuong.com.manga.usecase

import io.reactivex.Single

interface CheckRecentFavoriteMigrationNeededUseCase {
    fun execute(): Single<Boolean>
}