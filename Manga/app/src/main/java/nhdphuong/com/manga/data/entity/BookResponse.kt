package nhdphuong.com.manga.data.entity

import nhdphuong.com.manga.data.entity.book.Book

sealed class BookResponse {
    data class Success(val book: Book) : BookResponse()

    data class Failure(val error: Throwable) : BookResponse()
}
