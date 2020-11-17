package nhdphuong.com.manga.features.comment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.comment.Comment
import nhdphuong.com.manga.supports.SpaceItemDecoration
import nhdphuong.com.manga.views.adapters.CommentAdapter
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.doOnScrollToBottom
import nhdphuong.com.manga.views.doOnScrolled
import javax.inject.Inject


class CommentThreadActivity : AppCompatActivity(), CommentThreadContract.View {
    @Inject
    lateinit var presenter: CommentThreadContract.Presenter

    private var commentAdapter: CommentAdapter? = null

    private lateinit var loadingDialog: Dialog

    private lateinit var ibScrollToTop: ImageButton
    private lateinit var rvCommentList: RecyclerView
    private lateinit var ibBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment_thread)
        setUpUI()
        intent?.getStringExtra(BOOK_ID).orEmpty().let {
            NHentaiApp.instance.applicationComponent
                .plus(CommentThreadModule(this, it))
                .inject(this)
        }

        loadingDialog = createLoadingDialog()

        ibBack.setOnClickListener {
            onBackPressed()
        }

        presenter.start()
    }

    override fun setUpCommentList(commentList: List<Comment>, pageSize: Int) {
        rvCommentList.let {
            commentAdapter = CommentAdapter(commentList)
            it.adapter = commentAdapter
            it.isNestedScrollingEnabled = false
            val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            it.layoutManager = linearLayoutManager
            it.addItemDecoration(
                SpaceItemDecoration(
                    this,
                    R.dimen.space_medium,
                    showFirstDivider = true,
                    showLastDivider = true
                )
            )
            val commentCount = commentList.size
            it.becomeVisibleIf(commentCount > 0)
            ibScrollToTop.setOnClickListener { _ ->
                val firstVisiblePos = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                if (firstVisiblePos > pageSize) {
                    it.scrollToPosition(pageSize)
                    it.smoothScrollToPosition(0)
                } else {
                    it.smoothScrollToPosition(0)
                }
            }
            it.doOnScrolled {
                val firstVisiblePos = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                Logger.d("CommentThreadActivity", "firstVisiblePos=$firstVisiblePos")
                ibScrollToTop.becomeVisibleIf(firstVisiblePos > pageSize)
            }

            if (commentCount > 0) {
                it.doOnScrollToBottom(PREFETCH_COMMENTS_DISTANCE, this::prefetchCommentList)
            }
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

    private fun setUpUI() {
        ibScrollToTop = findViewById(R.id.ibScrollToTop)
        rvCommentList = findViewById(R.id.rvCommentList)
        ibBack = findViewById(R.id.ibBack)
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
