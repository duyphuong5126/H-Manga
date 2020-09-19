package nhdphuong.com.manga.features.comment

import dagger.Module
import dagger.Provides

@Module
class CommentThreadModule(
    private val commentThreadView: CommentThreadContract.View,
    private val bookId: String
) {
    @Provides
    fun providesCommentThreadView(): CommentThreadContract.View = commentThreadView

    @Provides
    fun providesBookId(): String = bookId

    @Provides
    fun providesCommentThreadPresenter(
        commentThreadPresenter: CommentThreadPresenter
    ): CommentThreadContract.Presenter = commentThreadPresenter
}
