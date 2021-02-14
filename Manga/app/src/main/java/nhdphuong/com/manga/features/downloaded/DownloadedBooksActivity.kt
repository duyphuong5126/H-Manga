package nhdphuong.com.manga.features.downloaded

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.becomeInvisible
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView
import javax.inject.Inject

class DownloadedBooksActivity : AppCompatActivity(),
    DownloadedBooksContract.View,
    PtrUIHandler,
    View.OnClickListener {
    @Inject
    lateinit var downloadedBooksPresenter: DownloadedBooksContract.Presenter

    private lateinit var loadingDialog: Dialog
    private lateinit var bookListAdapter: BookAdapter
    private lateinit var paginationAdapter: PaginationAdapter

    private val updateDotsHandler: Handler = Handler(Looper.getMainLooper())

    private lateinit var ibSwitch: ImageButton
    private lateinit var mtvTitle: MyTextView
    private lateinit var srlPullToReload: PtrFrameLayout
    private lateinit var ibBack: ImageButton
    private lateinit var btnFirst: ImageView
    private lateinit var btnLast: ImageView
    private lateinit var rvBookList: RecyclerView
    private lateinit var rvPagination: RecyclerView
    private lateinit var refreshHeader: View
    private lateinit var clNothing: ConstraintLayout
    private lateinit var mbReload: MyButton
    private lateinit var tvNothing: MyTextView
    private lateinit var ivRefresh: ImageView
    private lateinit var mtvLastUpdate: MyTextView
    private lateinit var mtvRefresh: MyTextView
    private lateinit var pbRefresh: ProgressBar

    private var downloadedBooks = ""
    private var noDownloadedBooks = ""
    private var lastUpdate = ""
    private var updated = ""
    private var releaseToRefresh = ""
    private var updatingTemplate = ""
    private var pullDown = ""

    private fun setUpUI() {
        ibSwitch = findViewById(R.id.ibSwitch)
        mtvTitle = findViewById(R.id.mtvTitle)
        btnFirst = findViewById(R.id.btnFirst)
        btnLast = findViewById(R.id.btnLast)
        ibBack = findViewById(R.id.ibBack)
        refreshHeader = findViewById(R.id.refreshHeader)
        rvBookList = findViewById(R.id.rvBookList)
        rvPagination = findViewById(R.id.rvPagination)
        srlPullToReload = findViewById(R.id.srlPullToReload)
        clNothing = findViewById(R.id.clNothing)
        mbReload = findViewById(R.id.mbReload)
        tvNothing = findViewById(R.id.tvNothing)
        ivRefresh = refreshHeader.findViewById(R.id.ivRefresh)
        mtvLastUpdate = refreshHeader.findViewById(R.id.mtvLastUpdate)
        mtvRefresh = refreshHeader.findViewById(R.id.mtvRefresh)
        pbRefresh = refreshHeader.findViewById(R.id.pbRefresh)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_special_book_list)
        NHentaiApp.instance.applicationComponent.plus(DownloadedBooksModule(this))
            .inject(this)

        downloadedBooks = getString(R.string.downloaded_books)
        noDownloadedBooks = getString(R.string.no_downloaded_book)
        lastUpdate = getString(R.string.last_update)
        updated = getString(R.string.updated)
        releaseToRefresh = getString(R.string.release_to_refresh)
        updatingTemplate = getString(R.string.updating)
        pullDown = getString(R.string.pull_down)

        setUpUI()

        loadingDialog = createLoadingDialog()
        ibSwitch.becomeInvisible()
        mtvTitle.text = downloadedBooks

        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                srlPullToReload.postDelayed(srlPullToReload::refreshComplete, 1000)
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })

        ibBack.setOnClickListener(this)
        btnFirst.setOnClickListener(this)
        btnLast.setOnClickListener(this)
        mbReload.gone()
        tvNothing.text = noDownloadedBooks
        downloadedBooksPresenter.start()
    }

    override fun onStart() {
        super.onStart()
        if (this::bookListAdapter.isInitialized) {
            downloadedBooksPresenter.reloadBookMarkers()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.DOWNLOADED_DATA_PREVIEW_REQUEST) {
            if (data?.action == Constants.REFRESH_DOWNLOADED_BOOK_LIST) {
                data.getStringExtra(Constants.BOOK_ID)?.takeIf { it.isNotBlank() }?.let { bookId ->
                    downloadedBooksPresenter.notifyBookRemoved(bookId)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadedBooksPresenter.stop()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibBack -> {
                onBackPressed()
            }

            R.id.btnFirst -> {
                downloadedBooksPresenter.jumToFirstPage()
                paginationAdapter.jumpToFirst()
                jumpTo(0)
            }

            R.id.btnLast -> {
                downloadedBooksPresenter.jumToLastPage()
                paginationAdapter.jumpToLast()
                jumpTo(paginationAdapter.itemCount - 1)
            }
        }
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
        val spanCount = if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        val bookListLayoutManager = object : StaggeredGridLayoutManager(spanCount, VERTICAL) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        bookListLayoutManager.gapStrategy = StaggeredGridLayoutManager.HORIZONTAL
        rvBookList.layoutManager = bookListLayoutManager
        rvBookList.adapter = bookListAdapter
        rvBookList.doOnGlobalLayout(downloadedBooksPresenter::reloadBookMarkers)
    }

    override fun refreshBookList() {
        bookListAdapter.notifyDataSetChanged()
        val listNotEmpty = bookListAdapter.itemCount > 0
        if (listNotEmpty) {
            downloadedBooksPresenter.reloadBookMarkers()
            downloadedBooksPresenter.reloadBookThumbnails()
        }
        clNothing.becomeVisibleIf(!listNotEmpty)
    }

    override fun refreshThumbnailList(thumbnailList: List<Pair<String, String>>) {
        bookListAdapter.publishDownloadedThumbnails(thumbnailList)
    }

    override fun showNothingView() {
        clNothing.becomeVisible()
    }

    override fun refreshRecentPagination(pageCount: Int) {
        val recentPagination = rvPagination
        if (pageCount == 0) {
            btnFirst.gone()
            btnLast.gone()
            recentPagination.gone()
            return
        }
        paginationAdapter = PaginationAdapter(pageCount)
        paginationAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                downloadedBooksPresenter.jumpToPage(page)
            }
        }
        recentPagination.becomeVisible()
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recentPagination.layoutManager = layoutManager
        recentPagination.adapter = paginationAdapter
        recentPagination.doOnGlobalLayout {
            val lastVisiblePageItem = layoutManager.findLastVisibleItemPosition()
            btnFirst.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
            btnLast.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
        }
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(lastUpdate, lastRefreshTimeStamp)
        mtvLastUpdate.text = lastRefresh
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
        return currentState != Lifecycle.State.DESTROYED
    }

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        endUpdateDotsTask()
        mtvRefresh.text = updated
        downloadedBooksPresenter.reloadLastBookListRefreshTime()
        ivRefresh.rotation = 0F
        ivRefresh.becomeVisible()
        pbRefresh.gone()
    }

    override fun onUIPositionChange(
        frame: PtrFrameLayout?,
        isUnderTouch: Boolean,
        status: Byte,
        ptrIndicator: PtrIndicator?
    ) {
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            mtvRefresh.text = releaseToRefresh
            ivRefresh.rotation = 180F
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        ivRefresh.gone()
        pbRefresh.becomeVisible()
        mtvRefresh.text = String.format(updatingTemplate, "")
        runUpdateDotsTask()
    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        downloadedBooksPresenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        mtvRefresh.text = pullDown
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        var currentPos = 0
        val updateDotsTask = {
            val dotsArray = resources.getStringArray(R.array.dots)
            mtvRefresh.text =
                String.format(updatingTemplate, dotsArray[currentPos])
            if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
        }
        val runnable = object : Runnable {
            override fun run() {
                updateDotsTask()
                updateDotsHandler.postDelayed(this, 500)
            }
        }
        updateDotsHandler.post(runnable)
    }

    private fun endUpdateDotsTask() {
        updateDotsHandler.removeCallbacksAndMessages(null)
    }

    private fun jumpTo(pageNumber: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            rvPagination.scrollToPosition(pageNumber)
        }
    }

    companion object {
        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3

        @JvmStatic
        fun start(fromContext: Context) {
            val intent = Intent(fromContext, DownloadedBooksActivity::class.java)
            fromContext.startActivity(intent)
        }
    }
}
