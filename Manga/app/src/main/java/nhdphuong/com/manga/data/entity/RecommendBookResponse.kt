package nhdphuong.com.manga.data.entity

import nhdphuong.com.manga.data.entity.book.RecommendBook

sealed class RecommendBookResponse {
    data class Success(val recommendBook: RecommendBook) : RecommendBookResponse()

    data class Failure(val error: Throwable) : RecommendBookResponse()
}
