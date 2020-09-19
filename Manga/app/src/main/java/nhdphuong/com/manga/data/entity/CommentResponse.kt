package nhdphuong.com.manga.data.entity

import nhdphuong.com.manga.data.entity.comment.Comment

sealed class CommentResponse {
    data class Success(val commentList: List<Comment>) : CommentResponse()

    data class Failure(val error: Throwable) : CommentResponse()
}
