package nhdphuong.com.manga.features.downloaded

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.layout_refresh_header.view.ivRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.pbRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvLastUpdate
import kotlinx.android.synthetic.main.layout_special_book_list.ibSwitch
import kotlinx.android.synthetic.main.layout_special_book_list.mtvTitle
import kotlinx.android.synthetic.main.layout_special_book_list.srlPullToReload
import kotlinx.android.synthetic.main.layout_special_book_list.ibBack
import kotlinx.android.synthetic.main.layout_special_book_list.btnFirst
import kotlinx.android.synthetic.main.layout_special_book_list.btnLast
import kotlinx.android.synthetic.main.layout_special_book_list.rvBookList
import kotlinx.android.synthetic.main.layout_special_book_list.rvPagination
import kotlinx.android.synthetic.main.layout_special_book_list.refreshHeader
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.becomeInvisible
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import javax.inject.Inject

class DownloadedBooksActivity : AppCompatActivity(), DownloadedBooksContract.View, PtrUIHandler {
    @Inject
    lateinit var downloadedBooksPresenter: DownloadedBooksPresenter

    private lateinit var loadingDialog: Dialog
    private lateinit var bookListAdapter: BookAdapter
    private lateinit var paginationAdapter: PaginationAdapter

    private lateinit var updateDotsHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_special_book_list)
        NHentaiApp.instance.applicationComponent.plus(DownloadedBooksModule(this))
            .inject(this)

        loadingDialog = DialogHelper.createLoadingDialog(this)
        ibSwitch.becomeInvisible()
        mtvTitle.text = getString(R.string.downloaded_books)

        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                frame?.postDelayed({
                    srlPullToReload.refreshComplete()
                }, 1000)
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })

        ibBack.setOnClickListener {
            onBackPressed()
        }
        btnFirst.setOnClickListener {
            downloadedBooksPresenter.jumToFirstPage()
            paginationAdapter.jumpToFirst()
            jumpTo(0)
        }
        btnLast.setOnClickListener {
            downloadedBooksPresenter.jumToLastPage()
            paginationAdapter.jumpToLast()
            jumpTo(paginationAdapter.itemCount - 1)
        }
        downloadedBooksPresenter.start()
    }

    override fun onStart() {
        super.onStart()
        if (this::bookListAdapter.isInitialized) {
            downloadedBooksPresenter.reloadBookMarkers()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadedBooksPresenter.stop()
    }

    override fun setUpBookList(bookList: List<Book>) {
        bookListAdapter = BookAdapter(
            bookList,
            BookAdapter.HOME_PREVIEW_BOOK,
            object : BookAdapter.OnBookClick {
                override fun onItemClick(item: Book) {
                    BookPreviewActivity.startViewDownloadedData(this@DownloadedBooksActivity, item)
                }
            }
        )

        val isLandscape = resources.getBoolean(R.bool.is_landscape)
        val bookListLayoutManager = object : GridLayoutManager(
            this,
            if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        ) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        rvBookList.layoutManager = bookListLayoutManager
        rvBookList.adapter = bookListAdapter
        rvBookList.doOnGlobalLayout {
            downloadedBooksPresenter.reloadBookMarkers()
        }
    }

    override fun refreshBookList() {
        bookListAdapter.notifyDataSetChanged()
        downloadedBooksPresenter.reloadBookMarkers()
        downloadedBooksPresenter.reloadBookThumbnails()
    }

    override fun refreshThumbnailList(thumbnailList: List<Pair<String, String>>) {
        bookListAdapter.publishDownloadedThumbnails(thumbnailList)
    }

    override fun refreshRecentPagination(pageCount: Int) {
        val recentPagination = rvPagination
        if (pageCount == 0) {
            btnFirst.gone()
            btnLast.gone()
            recentPagination.gone()
            return
        }
        paginationAdapter = PaginationAdapter(this, pageCount)
        paginationAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                Logger.d(TAG, "Page $page is selected")
                downloadedBooksPresenter.jumpToPage(page)
            }
        }
        recentPagination.visibility = View.VISIBLE
        recentPagination.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentPagination.adapter = paginationAdapter
        recentPagination.doOnGlobalLayout {
            btnFirst.becomeVisibleIf(paginationAdapter.maxVisible < pageCount - 1)
            btnLast.becomeVisibleIf(paginationAdapter.maxVisible < pageCount - 1)
        }
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(getString(R.string.last_update), lastRefreshTimeStamp)
        refreshHeader.mtvLastUpdate.text = lastRefresh
    }

    override fun showRecentBooks(recentList: List<String>) {
        bookListAdapter.setRecentList(recentList)
    }

    override fun showFavoriteBooks(favoriteList: List<String>) {
        bookListAdapter.setFavoriteList(favoriteList)
    }

    override fun showLoading() {
        if (isActive()) {
            loadingDialog.show()
        }
    }

    override fun hideLoading() {
        if (isActive()) {
            loadingDialog.dismiss()
        }
    }

    override fun isActive(): Boolean {
        val currentState = lifecycle.currentState
        return currentState == Lifecycle.State.STARTED || currentState == Lifecycle.State.RESUMED
    }

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        endUpdateDotsTask()
        refreshHeader.mtvRefresh.text = getString(R.string.updated)
        downloadedBooksPresenter.reloadLastBookListRefreshTime()
        refreshHeader.ivRefresh.rotation = 0F
        refreshHeader.ivRefresh.visibility = View.VISIBLE
        refreshHeader.pbRefresh.visibility = View.GONE
    }

    override fun onUIPositionChange(
        frame: PtrFrameLayout?,
        isUnderTouch: Boolean,
        status: Byte,
        ptrIndicator: PtrIndicator?
    ) {
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            refreshHeader.mtvRefresh.text = getString(R.string.release_to_refresh)
            refreshHeader.ivRefresh.rotation = 180F
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        refreshHeader.ivRefresh.visibility = View.GONE
        refreshHeader.pbRefresh.visibility = View.VISIBLE
        refreshHeader.mtvRefresh.text = String.format(getString(R.string.updating), "")
        runUpdateDotsTask()
    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        //mPresenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        refreshHeader.mtvRefresh.text = getString(R.string.pull_down)
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        updateDotsHandler = Handler()
        var currentPos = 0
        val updateDotsTask = {
            val dotsArray = resources.getStringArray(R.array.dots)
            val loadingString = getString(R.string.updating)
            Logger.d("Dialog", "Current pos: $currentPos")
            refreshHeader.mtvRefresh.text =
                String.format(loadingString, dotsArray[currentPos])
            if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
        }
        val runnable = object : Runnable {
            override fun run() {
                updateDotsTask()
                updateDotsHandler.postDelayed(this, 500)
            }
        }
        runnable.run()
    }

    private fun endUpdateDotsTask() {
        if (this::updateDotsHandler.isInitialized) {
            updateDotsHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun jumpTo(pageNumber: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            rvPagination.scrollToPosition(pageNumber)
        }
    }

    companion object {
        private const val TAG = "DownloadedBooksActivity"
        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, DownloadedBooksActivity::class.java)
            fromContext.startActivity(intent)
        }
    }
}
