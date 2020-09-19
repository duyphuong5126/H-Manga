package nhdphuong.com.manga.features.comment

import nhdphuong.com.manga.Base
import nhdphuong.com.manga.data.entity.comment.Comment

interface CommentThreadContract {
    interface View : Base.View<Presenter> {
        fun setUpCommentList(commentList: List<Comment>, pageSize: Int)
        fun showMoreCommentList(commentList: List<Comment>)
    }

    interface Presenter : Base.Presenter {
        fun syncNextPageOfCommentList(currentCommentCount: Int)
    }
}
