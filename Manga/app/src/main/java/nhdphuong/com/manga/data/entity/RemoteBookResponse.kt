package nhdphuong.com.manga.data.entity

import nhdphuong.com.manga.data.entity.book.RemoteBook

sealed class RemoteBookResponse {
    data class Success(val remoteBook: RemoteBook) : RemoteBookResponse()

    data class Failure(val error: Throwable) : RemoteBookResponse()
}
