package nhdphuong.com.manga.usecase

import io.reactivex.Observable
import nhdphuong.com.manga.data.entity.RecentFavoriteMigrationResult

interface RecentFavoriteMigrationUseCase {
    fun execute(): Observable<RecentFavoriteMigrationResult>
}