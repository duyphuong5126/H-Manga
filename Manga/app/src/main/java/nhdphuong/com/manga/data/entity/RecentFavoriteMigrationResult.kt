package nhdphuong.com.manga.data.entity

sealed class RecentFavoriteMigrationResult {
    data class MigratedBook(
        val progress: Int,
        val total: Int
    ) : RecentFavoriteMigrationResult()

    data class BookMigrationError(
        val failedItems: Int,
        val total: Int
    ) : RecentFavoriteMigrationResult()
}
