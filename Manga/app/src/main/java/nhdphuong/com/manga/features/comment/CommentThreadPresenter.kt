package nhdphuong.com.manga.features.comment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.data.entity.CommentResponse
import nhdphuong.com.manga.data.entity.comment.Comment
import nhdphuong.com.manga.data.repository.BookRepository
import nhdphuong.com.manga.scope.corountine.IO
import nhdphuong.com.manga.scope.corountine.Main
import javax.inject.Inject

class CommentThreadPresenter @Inject constructor(
    private val bookId: String,
    private val bookRepository: BookRepository,
    private val view: CommentThreadContract.View,
    @IO private val io: CoroutineScope,
    @Main private val main: CoroutineScope
) : CommentThreadContract.Presenter {
    private val commentSource = ArrayList<Comment>()

    override fun start() {
        view.showLoading()
        io.launch {
            when (val commentResponse = bookRepository.getCommentList(bookId)) {
                is CommentResponse.Success -> {
                    Logger.d(TAG, "commentList=${commentResponse.commentList.size}")
                    val startPosition = 0
                    val desiredEndPosition = COMMENTS_PER_PAGE
                    val endPosition = minOf(commentResponse.commentList.size, desiredEndPosition)
                    val firstPage = if (endPosition > startPosition) {
                        commentResponse.commentList.subList(startPosition, endPosition)
                    } else emptyList()
                    main.launch {
                        commentSource.addAll(commentResponse.commentList)
                        if (view.isActive() && firstPage.isNotEmpty()) {
                            view.setUpCommentList(
                                firstPage,
                                COMMENTS_PER_PAGE
                            )
                        }
                        view.hideLoading()
                    }
                }

                is CommentResponse.Failure -> {
                    Logger.d(TAG, "failed to get comment list with error: ${commentResponse.error}")
                }
            }
        }
    }

    override fun syncNextPageOfCommentList(currentCommentCount: Int) {
        if (currentCommentCount < COMMENTS_PER_PAGE || currentCommentCount % COMMENTS_PER_PAGE > 0) {
            return
        }
        val page = currentCommentCount / COMMENTS_PER_PAGE
        val startPosition = page * COMMENTS_PER_PAGE
        val desiredEndPosition = (page + 1) * COMMENTS_PER_PAGE
        val endPosition = minOf(commentSource.size, desiredEndPosition)

        Logger.d(TAG, "startPosition=$startPosition, endPosition=$endPosition")
        if (endPosition > startPosition) {
            view.showMoreCommentList(commentSource.subList(startPosition, endPosition))
        }
    }

    override fun stop() {

    }

    companion object {
        private const val COMMENTS_PER_PAGE = 25
        private const val TAG = "CommentThreadPresenter"
    }
}
