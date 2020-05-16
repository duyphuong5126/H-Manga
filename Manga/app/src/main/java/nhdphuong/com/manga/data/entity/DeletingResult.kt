package nhdphuong.com.manga.data.entity

sealed class DeletingResult {
    abstract val bookId: String

    data class DeletingProgress(
        override val bookId: String, val progress: Int, val total: Int
    ) : DeletingResult()

    data class DeletingFailure(
        override val bookId: String, val fileUrl: String, val error: Throwable
    ) : DeletingResult()
}
