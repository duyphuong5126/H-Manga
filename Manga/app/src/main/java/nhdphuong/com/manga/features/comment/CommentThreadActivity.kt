package nhdphuong.com.manga.features.comment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_comment_thread.ibScrollToTop
import kotlinx.android.synthetic.main.activity_comment_thread.rvCommentList
import kotlinx.android.synthetic.main.activity_comment_thread.ibBack
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.comment.Comment
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.CommentAdapter
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.doOnScrollToBottom
import javax.inject.Inject


class CommentThreadActivity : AppCompatActivity(), CommentThreadContract.View {
    @Inject
    lateinit var presenter: CommentThreadContract.Presenter

    private var commentAdapter: CommentAdapter? = null

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_thread)
        intent?.getStringExtra(BOOK_ID).orEmpty().let {
            NHentaiApp.instance.applicationComponent
                .plus(CommentThreadModule(this, it))
                .inject(this)
        }

        val loadingTitle = getString(R.string.loading)
        loadingDialog = DialogHelper.createLoadingDialog(this, loadingTitle)

        ibBack?.setOnClickListener {
            onBackPressed()
        }

        ibScrollToTop?.setOnClickListener {
            rvCommentList?.scrollToPosition(0)
        }

        presenter.start()
    }

    override fun setUpCommentList(commentList: List<Comment>, pageSize: Int) {
        commentAdapter = CommentAdapter(commentList)
        rvCommentList?.adapter = commentAdapter
        rvCommentList?.isNestedScrollingEnabled = false
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCommentList?.layoutManager = linearLayoutManager
        rvCommentList?.addItemDecoration(
            SpaceItemDecoration(
                this,
                R.dimen.space_medium,
                showFirstDivider = true,
                showLastDivider = true
            )
        )
        val hasComments = commentList.isNotEmpty()
        rvCommentList?.becomeVisibleIf(hasComments)
        if (hasComments) {
            rvCommentList?.doOnScrollToBottom(PREFETCH_COMMENTS_DISTANCE, this::prefetchCommentList)
        }
    }

    override fun showMoreCommentList(commentList: List<Comment>) {
        if (commentList.isEmpty()) {
            return
        }
        commentAdapter?.addNewComments(commentList)
    }

    override fun showLoading() {
        loadingDialog.show()
    }

    override fun hideLoading() {
        loadingDialog.dismiss()
    }

    override fun isActive(): Boolean {
        val currentState = lifecycle.currentState
        return currentState != Lifecycle.State.DESTROYED
    }

    private fun prefetchCommentList() {
        presenter.syncNextPageOfCommentList(commentAdapter?.itemCount ?: 0)
    }

    companion object {
        private const val BOOK_ID = "bookId"
        private const val PREFETCH_COMMENTS_DISTANCE = 10

        @JvmStatic
        fun start(fromContext: Context, bookId: String) {
            val intent = Intent(fromContext, CommentThreadActivity::class.java)
            intent.putExtra(BOOK_ID, bookId)
            fromContext.startActivity(intent)
        }
    }
}
