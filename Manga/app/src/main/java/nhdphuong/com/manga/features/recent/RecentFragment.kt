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
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import kotlinx.android.synthetic.main.fragment_recent_list.btnFirst
import kotlinx.android.synthetic.main.fragment_recent_list.btnLast
import kotlinx.android.synthetic.main.fragment_recent_list.clNavigation
import kotlinx.android.synthetic.main.fragment_recent_list.ibBack
import kotlinx.android.synthetic.main.fragment_recent_list.ibSwitch
import kotlinx.android.synthetic.main.fragment_recent_list.mtvRecentTitle
import kotlinx.android.synthetic.main.fragment_recent_list.refreshHeader
import kotlinx.android.synthetic.main.fragment_recent_list.rvMainList
import kotlinx.android.synthetic.main.fragment_recent_list.rvPagination
import kotlinx.android.synthetic.main.fragment_recent_list.srlPullToReload
import kotlinx.android.synthetic.main.layout_refresh_header.view.ivRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvLastUpdate
import kotlinx.android.synthetic.main.layout_refresh_header.view.mtvRefresh
import kotlinx.android.synthetic.main.layout_refresh_header.view.pbRefresh
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Logger
import nhdphuong.com.manga.R
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.views.DialogHelper
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import java.util.Locale

/*
 * Created by nhdphuong on 6/10/18.
 */
class RecentFragment : Fragment(), RecentContract.View, PtrUIHandler {
    companion object {
        private const val TAG = "RecentFragment"

        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3
    }

    private lateinit var mPresenter: RecentContract.Presenter
    private lateinit var mRecentListAdapter: BookAdapter
    private lateinit var mPaginationAdapter: PaginationAdapter
    private lateinit var mLoadingDialog: Dialog

    private lateinit var mUpdateDotsHandler: Handler

    override fun setPresenter(presenter: RecentContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recent_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recentType = activity?.intent?.extras?.getString(
                Constants.RECENT_TYPE, Constants.RECENT
        ) ?: Constants.RECENT

        mLoadingDialog = DialogHelper.showLoadingDialog(activity!!)
        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                frame?.postDelayed({
                    srlPullToReload.refreshComplete()
                    RecentActivity.restart(recentType)
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

        mtvRecentTitle.text = getString(
                if (recentType == Constants.RECENT) R.string.recent else R.string.favorite
        ).toUpperCase(Locale.US)

        ibSwitch.setImageResource(
                if (recentType == Constants.RECENT) {
                    R.drawable.ic_recent_white
                } else {
                    R.drawable.ic_heart_white
                }
        )

        ibSwitch.setOnClickListener {
            RecentActivity.restart(
                    if (recentType == Constants.RECENT) {
                        Constants.FAVORITE
                    } else {
                        Constants.RECENT
                    })
        }
        ibBack.setOnClickListener {
            activity?.onBackPressed()
        }
        btnFirst.setOnClickListener {
            mPresenter.jumToFirstPage()
        }
        btnLast.setOnClickListener {
            mPresenter.jumToLastPage()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()

        val recentType = activity?.intent?.extras?.getString(
                Constants.RECENT_TYPE, Constants.RECENT
        ) ?: Constants.RECENT
        mPresenter.setType(recentType)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
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
        mPresenter.stop()
    }

    override fun setUpRecentBookList(recentBookList: List<Book>) {
        val recentFragment = this
        mRecentListAdapter = BookAdapter(
                recentBookList,
                BookAdapter.HOME_PREVIEW_BOOK,
                object : BookAdapter.OnBookClick {
                    override fun onItemClick(item: Book) {
                        BookPreviewActivity.start(recentFragment, item)
                    }
                }
        )
        val recentList: RecyclerView = rvMainList
        val isLandscape = resources.getBoolean(R.bool.is_landscape)
        val recentListLayoutManager = object : GridLayoutManager(
                context,
                if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        ) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        recentList.layoutManager = recentListLayoutManager
        recentList.adapter = mRecentListAdapter
    }

    override fun refreshRecentBookList() {
        mRecentListAdapter.notifyDataSetChanged()
        rvMainList.post {
            rvMainList.smoothScrollToPosition(0)
        }
        mPresenter.reloadRecentMarks()
    }

    override fun refreshRecentPagination(pageCount: Int) {
        val recentPagination = rvPagination
        if (pageCount == 0) {
            btnFirst.visibility = View.GONE
            btnLast.visibility = View.GONE
            recentPagination.visibility = View.GONE
            return
        }
        mPaginationAdapter = PaginationAdapter(context!!, pageCount)
        mPaginationAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                Logger.d(TAG, "Page $page is selected")
                mPresenter.jumpToPage(page)
            }
        }
        recentPagination.visibility = View.VISIBLE
        recentPagination.layoutManager = LinearLayoutManager(
                activity,
                LinearLayoutManager.HORIZONTAL,
                false
        )
        recentPagination.adapter = mPaginationAdapter
        recentPagination.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            @Suppress("DEPRECATION")
            override fun onGlobalLayout() {
                if (mPaginationAdapter.maxVisible >= pageCount - 1) {
                    btnFirst.visibility = View.GONE
                    btnLast.visibility = View.GONE
                } else {
                    btnFirst.visibility = View.VISIBLE
                    btnLast.visibility = View.VISIBLE
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    recentPagination.viewTreeObserver.removeGlobalOnLayoutListener(this)
                } else {
                    recentPagination.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    override fun showFavoriteBooks(favoriteList: List<Int>) {
        mRecentListAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<Int>) {
        mRecentListAdapter.setRecentList(recentList)
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(getString(R.string.last_update), lastRefreshTimeStamp)
        refreshHeader.mtvLastUpdate.text = lastRefresh
    }

    override fun showLoading() {
        if (isAdded) {
            mLoadingDialog.show()
            clNavigation.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        if (isAdded) {
            mLoadingDialog.dismiss()
            clNavigation.visibility = View.VISIBLE
        }
    }

    override fun isActive(): Boolean = isAdded

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshComplete")
        endUpdateDotsTask()
        refreshHeader.mtvRefresh.text = getString(R.string.updated)
        mPresenter.saveLastBookListRefreshTime()
        mPresenter.reloadLastBookListRefreshTime()
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
        Logger.d(TAG, "onUIPositionChange isUnderTouch: $isUnderTouch, status: $status, " +
                "over keep header: ${ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading}, " +
                "over refresh: ${ptrIndicator?.isOverOffsetToRefresh}")
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            refreshHeader.mtvRefresh.text = getString(R.string.release_to_refresh)
            refreshHeader.ivRefresh.rotation = 180F
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshBegin")
        refreshHeader.ivRefresh.visibility = View.GONE
        refreshHeader.pbRefresh.visibility = View.VISIBLE
        refreshHeader.mtvRefresh.text = String.format(getString(R.string.updating), "")
        runUpdateDotsTask()
    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshPrepare")
        mPresenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIReset")
        refreshHeader.mtvRefresh.text = getString(R.string.pull_down)
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        mUpdateDotsHandler = Handler()
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
                mUpdateDotsHandler.postDelayed(this, 500)
            }
        }
        runnable.run()
    }

    private fun endUpdateDotsTask() {
        if (this::mUpdateDotsHandler.isInitialized) {
            mUpdateDotsHandler.removeCallbacksAndMessages(null)
        }
    }
}
