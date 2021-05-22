package nhdphuong.com.manga.features.recent

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.enum.ErrorEnum
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView

/*
 * Created by nhdphuong on 6/10/18.
 */
class RecentFragment : Fragment(), RecentContract.View, PtrUIHandler, View.OnClickListener {
    companion object {
        private const val TAG = "RecentFragment"

        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3
        private const val REFRESHING_DELAY = 1000L
        private const val REFRESHING_ROTATION = 180F
        private const val DOT_TASK_DELAY = 500L
    }

    private lateinit var presenter: RecentContract.Presenter
    private lateinit var recentListAdapter: BookAdapter
    private lateinit var paginationAdapter: PaginationAdapter
    private lateinit var loadingDialog: Dialog


    private lateinit var btnFirst: ImageView
    private lateinit var btnLast: ImageView
    private lateinit var clNavigation: ConstraintLayout
    private lateinit var ibBack: ImageButton
    private lateinit var ibSwitch: ImageButton
    private lateinit var mtvTitle: MyTextView
    private lateinit var refreshHeader: View
    private lateinit var rvBookList: RecyclerView
    private lateinit var rvPagination: RecyclerView
    private lateinit var srlPullToReload: PtrFrameLayout
    private lateinit var clNothing: ConstraintLayout
    private lateinit var mbReload: MyButton
    private lateinit var tvNothing: MyTextView
    private lateinit var ivRefresh: ImageView
    private lateinit var mtvLastUpdate: MyTextView
    private lateinit var mtvRefresh: MyTextView
    private lateinit var pbRefresh: ProgressBar

    private val updateDotsHandler: Handler = Handler(Looper.getMainLooper())

    private val currentRecentType: String
        get() {
            return activity?.intent?.extras?.getString(
                Constants.RECENT_TYPE, Constants.RECENT
            ) ?: Constants.RECENT
        }

    override fun setPresenter(presenter: RecentContract.Presenter) {
        this.presenter = presenter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_special_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpUI(view)

        val recentType = currentRecentType
        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                srlPullToReload.postDelayed({
                    srlPullToReload.refreshComplete()
                    RecentActivity.restart(recentType)
                }, REFRESHING_DELAY)
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })

        mtvTitle.text = getString(
            if (recentType == Constants.RECENT) R.string.recent_screen_title else R.string.favorite
        )

        ibSwitch.setImageResource(
            if (recentType == Constants.RECENT) {
                R.drawable.ic_heart_white
            } else {
                R.drawable.ic_recent_white
            }
        )

        ibSwitch.setOnClickListener(this)
        ibBack.setOnClickListener(this)
        btnFirst.setOnClickListener(this)
        btnLast.setOnClickListener(this)
        mbReload.gone()

        activity?.let {
            loadingDialog = it.createLoadingDialog()
        }

        tvNothing.text = if (recentType == Constants.RECENT) {
            getString(R.string.no_recent_book)
        } else {
            getString(R.string.no_favorite_book)
        }
        presenter.setType(recentType)
        presenter.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.BOOK_PREVIEW_REQUEST && data?.action == Constants.RECENT_DATA_UPDATED_ACTION) {
            presenter.reloadRecentMarks()
            return
        }
        if (resultCode != Activity.RESULT_OK || data?.action != Constants.TAG_SELECTED_ACTION) {
            return
        }
        val tagName = data.getStringExtra(Constants.SELECTED_TAG).orEmpty()
        activity?.run {
            val intent = intent
            intent.action = Constants.TAG_SELECTED_ACTION
            intent.putExtra(Constants.SELECTED_TAG, tagName)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stop()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ibSwitch -> {
                RecentActivity.restart(
                    if (currentRecentType == Constants.RECENT) {
                        Constants.FAVORITE
                    } else {
                        Constants.RECENT
                    }
                )
            }

            R.id.ibBack -> {
                activity?.onBackPressed()
            }

            R.id.btnFirst -> {
                presenter.jumToFirstPage()
                paginationAdapter.jumpToFirst()
                jumpTo(0)
            }

            R.id.btnLast -> {
                presenter.jumToLastPage()
                paginationAdapter.jumpToLast()
                jumpTo(paginationAdapter.itemCount - 1)
            }
        }
    }

    override fun setUpRecentBookList(recentBookList: List<Book>) {
        val recentFragment = this
        recentListAdapter = BookAdapter(
            recentBookList,
            BookAdapter.HOME_PREVIEW_BOOK,
            object : BookAdapter.OnBookClick {
                override fun onItemClick(item: Book) {
                    BookPreviewActivity.start(recentFragment, item)
                }
            }
        )
        val recentList: RecyclerView = rvBookList
        val isLandscape = resources.getBoolean(R.bool.is_landscape)
        val spanCount = if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        val recentListLayoutManager = object : StaggeredGridLayoutManager(spanCount, VERTICAL) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        recentListLayoutManager.gapStrategy = StaggeredGridLayoutManager.HORIZONTAL
        recentList.layoutManager = recentListLayoutManager
        recentList.adapter = recentListAdapter
    }

    override fun refreshRecentBookList() {
        clNothing.gone()
        recentListAdapter.notifyDataSetChanged()
        rvBookList.post {
            rvBookList.smoothScrollToPosition(0)
        }
        presenter.reloadRecentMarks()
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
                Logger.d(TAG, "Page $page is selected")
                presenter.jumpToPage(page)
            }
        }
        recentPagination.becomeVisible()
        val layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recentPagination.layoutManager = layoutManager
        recentPagination.adapter = paginationAdapter
        recentPagination.doOnGlobalLayout {
            val lastVisiblePageItem = layoutManager.findLastVisibleItemPosition()
            btnFirst.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
            btnLast.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
        }
    }

    override fun showFavoriteBooks(favoriteList: List<String>) {
        recentListAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<String>) {
        recentListAdapter.setRecentList(recentList)
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(getString(R.string.last_update), lastRefreshTimeStamp)
        mtvLastUpdate.text = lastRefresh
    }

    override fun showNothingView(@RecentType recentType: String) {
        clNothing.becomeVisible()
    }

    override fun updateErrorMessage(errorEnum: ErrorEnum) {
        val stringResId = when (errorEnum) {
            ErrorEnum.NetworkError -> R.string.internet_error
            ErrorEnum.DataParsingError -> R.string.library_error_data_parsing_label
            ErrorEnum.TimeOutError -> R.string.library_error_time_out_label
            ErrorEnum.UnknownError -> R.string.library_error_unknown_label
        }
        tvNothing.text = getString(stringResId)
    }

    override fun showLoading() {
        if (isAdded) {
            loadingDialog.show()
            clNavigation.gone()
        }
    }

    override fun hideLoading() {
        if (isAdded) {
            loadingDialog.dismiss()
            clNavigation.becomeVisible()
        }
    }

    override fun isActive(): Boolean = isAdded

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        endUpdateDotsTask()
        mtvRefresh.text = getString(R.string.updated)
        presenter.saveLastBookListRefreshTime()
        presenter.reloadLastBookListRefreshTime()
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
            mtvRefresh.text = getString(R.string.release_to_refresh)
            ivRefresh.rotation = REFRESHING_ROTATION
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        ivRefresh.gone()
        pbRefresh.becomeVisible()
        mtvRefresh.text = String.format(getString(R.string.updating), "")
        runUpdateDotsTask()
    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        presenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        mtvRefresh.text = getString(R.string.pull_down)
    }

    private fun setUpUI(rootView: View) {
        btnFirst = rootView.findViewById(R.id.btnFirst)
        btnLast = rootView.findViewById(R.id.btnLast)
        clNavigation = rootView.findViewById(R.id.clNavigation)
        ibBack = rootView.findViewById(R.id.ibBack)
        ibSwitch = rootView.findViewById(R.id.ibSwitch)
        mtvTitle = rootView.findViewById(R.id.mtvTitle)
        refreshHeader = rootView.findViewById(R.id.refreshHeader)
        rvBookList = rootView.findViewById(R.id.rvBookList)
        rvPagination = rootView.findViewById(R.id.rvPagination)
        srlPullToReload = rootView.findViewById(R.id.srlPullToReload)
        clNothing = rootView.findViewById(R.id.clNothing)
        mbReload = rootView.findViewById(R.id.mbReload)
        tvNothing = rootView.findViewById(R.id.tvNothing)
        ivRefresh = refreshHeader.findViewById(R.id.ivRefresh)
        mtvLastUpdate = refreshHeader.findViewById(R.id.mtvLastUpdate)
        mtvRefresh = refreshHeader.findViewById(R.id.mtvRefresh)
        pbRefresh = refreshHeader.findViewById(R.id.pbRefresh)
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        var currentPos = 0
        val updateDotsTask = {
            val dotsArray = resources.getStringArray(R.array.dots)
            val loadingString = getString(R.string.updating)
            mtvRefresh.text =
                String.format(loadingString, dotsArray[currentPos])
            if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
        }
        val runnable = object : Runnable {
            override fun run() {
                updateDotsTask()
                updateDotsHandler.postDelayed(this, DOT_TASK_DELAY)
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
}
