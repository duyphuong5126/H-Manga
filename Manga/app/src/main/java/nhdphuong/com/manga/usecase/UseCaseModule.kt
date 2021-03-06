package nhdphuong.com.manga.usecase

import dagger.Binds
import dagger.Module

@Module
interface UseCaseModule {
    @Binds
    fun startBookDownloadingUseCase(startBookDownloadingUseCaseImpl: StartBookDownloadingUseCaseImpl): StartBookDownloadingUseCase

    @Binds
    fun downloadBookUseCase(downloadBookUseCaseImpl: DownloadBookUseCaseImpl): DownloadBookUseCase

    @Binds
    fun startBookDeletingUseCase(startBookDeletingUseCaseImpl: StartBookDeletingUseCaseImpl): StartBookDeletingUseCase

    @Binds
    fun deleteBookUseCase(deleteBookUseCaseImpl: DeleteBookUseCaseImpl): DeleteBookUseCase

    @Binds
    fun getAllDownloadedBooksUseCase(getAllDownloadedBooksUseCase: GetAllDownloadedBooksUseCaseImpl): GetAllDownloadedBooksUseCase

    @Binds
    fun getDownloadedBookCoverUseCase(getDownloadedBookCoverUseCaseImpl: GetDownloadedBookCoverUseCaseImpl): GetDownloadedBookCoverUseCase

    @Binds
    fun getDownloadedBookPagesUseCase(getDownloadedBookPagesUseCaseImpl: GetDownloadedBookPagesUseCaseImpl): GetDownloadedBookPagesUseCase

    @Binds
    fun getAvailableBookThumbnailsUseCase(getAvailableBookThumbnailsUseCaseImpl: GetAvailableBookThumbnailsUseCaseImpl): GetAvailableBookThumbnailsUseCase

    @Binds
    fun getVersionCodeUseCase(getVersionCodeUseCaseImpl: GetVersionCodeUseCaseImpl): GetVersionCodeUseCase

    @Binds
    fun saveLastVisitedPageUseCase(saveLastVisitedPageUseCaseImpl: SaveLastVisitedPageUseCaseImpl): SaveLastVisitedPageUseCase

    @Binds
    fun getLastVisitedPageUseCase(getLastVisitedPageUseCaseImpl: GetLastVisitedPageUseCaseImpl): GetLastVisitedPageUseCase

    @Binds
    fun logAnalyticsErrorUseCase(analyticsErrorLogUseCaseImpl: LogAnalyticsErrorUseCaseImpl): LogAnalyticsErrorUseCase

    @Binds
    fun logAnalyticsEventUseCase(analyticsEventUseCaseImpl: LogAnalyticsEventUseCaseImpl): LogAnalyticsEventUseCase

    @Binds
    fun getAllSearchEntriesUseCase(getLatestSearchEntriesUseCaseImpl: GetLatestSearchEntriesUseCaseImpl): GetLatestSearchEntriesUseCase

    @Binds
    fun saveSearchInfoUseCase(saveSearchInfoUseCaseImpl: SaveSearchInfoUseCaseImpl): SaveSearchInfoUseCase

    @Binds
    fun recentFavoriteMigrationUseCase(useCaseImpl: RecentFavoriteMigrationUseCaseImpl): RecentFavoriteMigrationUseCase

    @Binds
    fun checkRecentFavoriteMigrationNeededUseCase(useCaseImpl: CheckRecentFavoriteMigrationNeededUseCaseImpl): CheckRecentFavoriteMigrationNeededUseCase

    @Binds
    fun getFeedbackFormUseCase(useCaseImpl: GetFeedbackFormUseCaseImpl): GetFeedbackFormUseCase

    @Binds
    fun getRecommendedSearchUseCase(useCaseImpl: GetRecommendedBooksUseCaseImpl): GetRecommendedBooksUseCase

    @Binds
    fun getRecommendedBooksFromFavoriteUseCase(useCaseImpl: GetRecommendedBooksFromFavoriteUseCaseImpl): GetRecommendedBooksFromFavoriteUseCase

    @Binds
    fun getRecommendedBooksFromReadingHistoryUseCase(useCaseImpl: GetRecommendedBooksFromReadingHistoryUseCaseImpl): GetRecommendedBooksFromReadingHistoryUseCase

    @Binds
    fun deleteSearchSuggestionUseCase(useCaseImpl: DeleteSearchSuggestionUseCaseImpl): DeleteSearchSuggestionUseCase

    @Binds
    fun removeBookFromPendingDownloadListUseCase(useCaseImpl: RemoveBookFromPendingDownloadListUseCaseImpl): RemoveBookFromPendingDownloadListUseCase

    @Binds
    fun getOldestPendingDownloadBookUseCase(useCaseImpl: GetOldestPendingDownloadBookUseCaseImpl): GetOldestPendingDownloadBookUseCase

    @Binds
    fun putBookIntoPendingDownloadListUseCase(useCaseImpl: PutBookIntoPendingDownloadListUseCaseImpl): PutBookIntoPendingDownloadListUseCase

    @Binds
    fun getPendingDownloadBookCountUseCase(useCaseImpl: GetPendingDownloadBookCountUseCaseImpl): GetPendingDownloadBookCountUseCase

    @Binds
    fun getPendingDownloadBookListUseCase(useCaseImpl: GetPendingDownloadBookListUseCaseImpl): GetPendingDownloadBookListUseCase

    @Binds
    fun recommendBooksByHistoryUseCase(useCaseImpl: RecommendBooksByHistoryUseCaseImpl): RecommendBooksByHistoryUseCase
}
