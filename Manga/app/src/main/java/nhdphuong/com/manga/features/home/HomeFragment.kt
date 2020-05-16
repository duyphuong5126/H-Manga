package nhdphuong.com.manga.features.home

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import kotlinx.android.synthetic.main.fragment_book_list.btnFirst
import kotlinx.android.synthetic.main.fragment_book_list.btnLast
import kotlinx.android.synthetic.main.fragment_book_list.clNavigation
import kotlinx.android.synthetic.main.fragment_book_list.clNothing
import kotlinx.android.synthetic.main.fragment_book_list.clReload
import kotlinx.android.synthetic.main.fragment_book_list.mtv_search_result
import kotlinx.android.synthetic.main.fragment_book_list.nsvMainList
import kotlinx.android.synthetic.main.fragment_book_list.refreshHeader
import kotlinx.android.synthetic.main.fragment_book_list.rvMainList
import kotlinx.android.synthetic.main.fragment_book_list.rvPagination
import kotlinx.android.synthetic.main.fragment_book_list.srlPullToReload
import kotlinx.android.synthetic.main.fragment_book_list.clUpgradePopup
import kotlinx.android.synthetic.main.fragment_book_list.ibUpgradePopupClose
import kotlinx.android.synthetic.main.fragment_book_list.mtvUpgradeTitle
import kotlinx.android.synthetic.main.fragment_book_list.upgradePopupPlaceHolder
import kotlinx.android.synthetic.main.layout_refresh_header.view.ivRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvLastUpdate
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.pbRefresh
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.R
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.supports.openUrl
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.PaginationAdapter

/*
 * Created by nhdphuong on 3/16/18.
 */
class HomeFragment : Fragment(), HomeContract.View, PtrUIHandler {

    private lateinit var homeListAdapter: BookAdapter
    private lateinit var homePaginationAdapter: PaginationAdapter
    private lateinit var homePresenter: HomeContract.Presenter
    private lateinit var loadingDialog: Dialog

    private val searchResultTitle: String = NHentaiApp.instance.getString(R.string.search_result)

    private lateinit var updateDotsHandler: Handler

    override fun setPresenter(presenter: HomeContract.Presenter) {
        homePresenter = presenter
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Logger.d(TAG, "onCreateView")
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated")

        nsvMainList.overScrollMode = View.OVER_SCROLL_NEVER
        btnFirst.setOnClickListener {
            homePresenter.jumToFirstPage()
            homePaginationAdapter.selectFirstPage()
            jumpTo(0)
        }
        btnLast.setOnClickListener {
            homePresenter.jumToLastPage()
            homePaginationAdapter.selectLastPage()
            jumpTo(homePaginationAdapter.itemCount - 1)
        }
        loadingDialog = DialogHelper.createLoadingDialog(activity!!)
        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                homePresenter.reloadCurrentPage {
                    frame?.postDelayed({
                        srlPullToReload.refreshComplete()
                    }, REFRESH_COMPLETE_DURATION)
                }
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })
        clReload.setOnClickListener {
            homePresenter.reloadCurrentPage {

            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Logger.d(TAG, "onActivityCreated")
        homePresenter.start()
        toggleSearchResult("")
        mtvUpgradeTitle.setOnClickListener {
            clUpgradePopup.gone()
            upgradePopupPlaceHolder.gone()
            homePresenter.setNewerVersionAcknowledged()
            context?.openUrl(REPOSITORY_URL)
        }
        ibUpgradePopupClose.setOnClickListener {
            clUpgradePopup.gone()
            upgradePopupPlaceHolder.gone()
            homePresenter.setNewerVersionAcknowledged()
        }
    }

    override fun onResume() {
        super.onResume()
        homePresenter.refreshAppVersion()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
        homePresenter.stop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Logger.d(TAG, "onConfigurationChanged")
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        val mainListLayoutManager = object : StaggeredGridLayoutManager(
            if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS,
            VERTICAL
        ) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        mainListLayoutManager.gapStrategy = StaggeredGridLayoutManager.HORIZONTAL
        rvMainList.layoutManager = mainListLayoutManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == Constants.BOOK_PREVIEW_REQUEST &&
            data?.action == Constants.RECENT_DATA_UPDATED_ACTION
        ) {
            homePresenter.reloadRecentBooks()
        }
    }

    @SuppressLint("PrivateResource")
    override fun setUpHomeBookList(homeBookList: List<Book>) {
        val homeFragment = this
        homeListAdapter = BookAdapter(
            homeBookList,
            BookAdapter.HOME_PREVIEW_BOOK,
            object : BookAdapter.OnBookClick {
                override fun onItemClick(item: Book) {
                    BookPreviewActivity.start(homeFragment, item)
                }
            })
        val mainList: RecyclerView = rvMainList
        val isLandscape = resources.getBoolean(R.bool.is_landscape)
        val mainListLayoutManager = object : StaggeredGridLayoutManager(
            if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS,
            VERTICAL
        ) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        mainListLayoutManager.gapStrategy = StaggeredGridLayoutManager.HORIZONTAL
        mainList.layoutManager = mainListLayoutManager
        mainList.adapter = homeListAdapter
    }

    override fun refreshHomeBookList() {
        homeListAdapter.notifyDataSetChanged()
        homePresenter.saveLastBookListRefreshTime()
        rvMainList.post {
            rvMainList.smoothScrollBy(0, 0)
        }
        homePresenter.reloadRecentBooks()
    }

    override fun refreshHomePagination(pageCount: Long) {
        val mainPagination = rvPagination
        if (pageCount == 0L) {
            btnFirst.gone()
            btnLast.gone()
            mainPagination.gone()
            return
        }
        homePaginationAdapter = PaginationAdapter(pageCount.toInt())
        homePaginationAdapter.onPageSelectCallback =
            object : PaginationAdapter.OnPageSelectCallback {
                override fun onPageSelected(page: Int) {
                    Logger.d(TAG, "Page $page is selected")
                    homePresenter.jumpToPage(page.toLong())
                }
            }
        mainPagination.becomeVisible()
        val layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mainPagination.layoutManager = layoutManager
        mainPagination.adapter = homePaginationAdapter
        mainPagination.doOnGlobalLayout {
            val lastVisiblePageItem = layoutManager.findLastVisibleItemPosition()
            btnFirst.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
            btnLast.becomeVisibleIf(lastVisiblePageItem < pageCount - 1)
        }
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(getString(R.string.last_update), lastRefreshTimeStamp)
        refreshHeader.mtvLastUpdate.text = lastRefresh
    }

    override fun showNothingView(isEmpty: Boolean) {
        clNothing.becomeVisibleIf(isEmpty)
    }

    override fun showRefreshingDialog() {
        DialogHelper.showBookListRefreshingDialog(activity!!) {

        }
    }

    override fun showFavoriteBooks(favoriteList: List<String>) {
        homeListAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<String>) {
        homeListAdapter.setRecentList(recentList)
    }

    override fun changeSearchResult(data: String) {
        toggleSearchResult(data)
    }

    override fun showBookPreview(book: Book) {
        BookPreviewActivity.start(this, book)
    }

    override fun startUpdateTagsService() {
        NHentaiApp.instance.startUpdateTagsService()
    }

    override fun showUpgradeNotification() {
        clUpgradePopup.becomeVisible()
        upgradePopupPlaceHolder.becomeVisible()
        clUpgradePopup.postDelayed({
            clUpgradePopup.gone()
            upgradePopupPlaceHolder.gone()
        }, APP_UPGRADE_TIME_OUT)
    }

    fun changeSearchInputted(data: String) {
        homePresenter.updateSearchData(data)
    }

    fun randomizeBook() {
        homePresenter.pickBookRandomly()
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
        Logger.d(TAG, "onUIRefreshComplete")
        endUpdateDotsTask()
        refreshHeader.mtvRefresh.text = getString(R.string.updated)
        homePresenter.saveLastBookListRefreshTime()
        homePresenter.reloadLastBookListRefreshTime()
        refreshHeader.ivRefresh.rotation = 0F
        refreshHeader.ivRefresh.becomeVisible()
        refreshHeader.pbRefresh.gone()
        updateDotsHandler.removeCallbacksAndMessages(null)
    }

    override fun onUIPositionChange(
        frame: PtrFrameLayout?,
        isUnderTouch: Boolean,
        status: Byte,
        ptrIndicator: PtrIndicator?
    ) {
        Logger.d(
            TAG, "onUIPositionChange isUnderTouch: $isUnderTouch, status: $status, " +
                    "over keep header: ${ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading}, " +
                    "over refresh: ${ptrIndicator?.isOverOffsetToRefresh}"
        )
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            refreshHeader.mtvRefresh.text = getString(R.string.release_to_refresh)
            refreshHeader.ivRefresh.rotation = REFRESH_HEADER_ANGEL
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshBegin")
        refreshHeader.ivRefresh.gone()
        refreshHeader.pbRefresh.becomeVisible()
        refreshHeader.mtvRefresh.text = String.format(getString(R.string.updating), "")
        runUpdateDotsTask()

    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshPrepare")
        homePresenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIReset")
        refreshHeader.mtvRefresh.text = getString(R.string.pull_down)
    }

    private fun jumpTo(pageNumber: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            rvPagination.scrollToPosition(pageNumber)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        updateDotsHandler = Handler()
        var currentPos = 0
        val updateDotsTask = {
            val dotsArray = resources.getStringArray(R.array.dots)
            val loadingString = getString(R.string.updating)
            Logger.d(TAG, "Current pos: $currentPos")
            refreshHeader.mtvRefresh.text =
                String.format(loadingString, dotsArray[currentPos])
            if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
        }
        val runnable = object : Runnable {
            override fun run() {
                updateDotsTask()
                updateDotsHandler.postDelayed(this, DOTS_UPDATE_INTERVAL)
            }
        }
        runnable.run()
    }

    private fun endUpdateDotsTask() {
        if (this::updateDotsHandler.isInitialized) {
            updateDotsHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun toggleSearchResult(data: String) {
        mtv_search_result.run {
            text = String.format(searchResultTitle, data)
            becomeVisibleIf(data.isNotBlank())
        }
    }

    companion object {
        private const val TAG = "HomeFragment"
        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3
        private const val DOTS_UPDATE_INTERVAL = 500L
        private const val REFRESH_COMPLETE_DURATION = 800L
        private const val REFRESH_HEADER_ANGEL = 180F
        private const val APP_UPGRADE_TIME_OUT = 2 * 60 * 1000L
        private const val REPOSITORY_URL = "https://github.com/duyphuong5126/H-Manga/releases"
    }
}
