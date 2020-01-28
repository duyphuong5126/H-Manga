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
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_special_book_list.btnFirst
import kotlinx.android.synthetic.main.layout_special_book_list.btnLast
import kotlinx.android.synthetic.main.layout_special_book_list.clNavigation
import kotlinx.android.synthetic.main.layout_special_book_list.ibBack
import kotlinx.android.synthetic.main.layout_special_book_list.ibSwitch
import kotlinx.android.synthetic.main.layout_special_book_list.mtvTitle
import kotlinx.android.synthetic.main.layout_special_book_list.refreshHeader
import kotlinx.android.synthetic.main.layout_special_book_list.rvBookList
import kotlinx.android.synthetic.main.layout_special_book_list.rvPagination
import kotlinx.android.synthetic.main.layout_special_book_list.srlPullToReload
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
        private const val REFRESHING_DELAY = 1000L
        private const val REFRESHING_ROTATION = 180F
        private const val DOT_TASK_DELAY = 500L
    }

    private lateinit var presenter: RecentContract.Presenter
    private lateinit var recentListAdapter: BookAdapter
    private lateinit var paginationAdapter: PaginationAdapter
    private lateinit var loadingDialog: Dialog

    private lateinit var updateDotsHandler: Handler

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

        val recentType = activity?.intent?.extras?.getString(
            Constants.RECENT_TYPE, Constants.RECENT
        ) ?: Constants.RECENT

        loadingDialog = DialogHelper.createLoadingDialog(activity!!)
        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                frame?.postDelayed({
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
            if (recentType == Constants.RECENT) R.string.recent else R.string.favorite
        ).toUpperCase(Locale.US)

        ibSwitch.setImageResource(
            if (recentType == Constants.RECENT) {
                R.drawable.ic_heart_white
            } else {
                R.drawable.ic_recent_white
            }
        )

        ibSwitch.setOnClickListener {
            RecentActivity.restart(
                if (recentType == Constants.RECENT) {
                    Constants.FAVORITE
                } else {
                    Constants.RECENT
                }
            )
        }
        ibBack.setOnClickListener {
            activity?.onBackPressed()
        }
        btnFirst.setOnClickListener {
            presenter.jumToFirstPage()
            paginationAdapter.jumpToFirst()
            jumpTo(0)
        }
        btnLast.setOnClickListener {
            presenter.jumToLastPage()
            paginationAdapter.jumpToLast()
            jumpTo(paginationAdapter.itemCount - 1)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        presenter.start()

        val recentType = activity?.intent?.extras?.getString(
            Constants.RECENT_TYPE, Constants.RECENT
        ) ?: Constants.RECENT
        presenter.setType(recentType)
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
        presenter.stop()
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
        val recentListLayoutManager = object : GridLayoutManager(
            context,
            if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        ) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        recentList.layoutManager = recentListLayoutManager
        recentList.adapter = recentListAdapter
    }

    override fun refreshRecentBookList() {
        recentListAdapter.notifyDataSetChanged()
        rvBookList.post {
            rvBookList.smoothScrollToPosition(0)
        }
        presenter.reloadRecentMarks()
    }

    override fun refreshRecentPagination(pageCount: Int) {
        val recentPagination = rvPagination
        if (pageCount == 0) {
            btnFirst.visibility = View.GONE
            btnLast.visibility = View.GONE
            recentPagination.visibility = View.GONE
            return
        }
        paginationAdapter = PaginationAdapter(context!!, pageCount)
        paginationAdapter.onPageSelectCallback = object : PaginationAdapter.OnPageSelectCallback {
            override fun onPageSelected(page: Int) {
                Logger.d(TAG, "Page $page is selected")
                presenter.jumpToPage(page)
            }
        }
        recentPagination.visibility = View.VISIBLE
        recentPagination.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recentPagination.adapter = paginationAdapter
        recentPagination.viewTreeObserver.addOnGlobalLayoutListener(object :
            OnGlobalLayoutListener {
            @Suppress("DEPRECATION")
            override fun onGlobalLayout() {
                if (paginationAdapter.maxVisible >= pageCount - 1) {
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

    override fun showFavoriteBooks(favoriteList: List<String>) {
        recentListAdapter.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<String>) {
        recentListAdapter.setRecentList(recentList)
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(getString(R.string.last_update), lastRefreshTimeStamp)
        refreshHeader.mtvLastUpdate.text = lastRefresh
    }

    override fun showLoading() {
        if (isAdded) {
            loadingDialog.show()
            clNavigation.visibility = View.GONE
        }
    }

    override fun hideLoading() {
        if (isAdded) {
            loadingDialog.dismiss()
            clNavigation.visibility = View.VISIBLE
        }
    }

    override fun isActive(): Boolean = isAdded

    override fun onUIRefreshComplete(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIRefreshComplete")
        endUpdateDotsTask()
        refreshHeader.mtvRefresh.text = getString(R.string.updated)
        presenter.saveLastBookListRefreshTime()
        presenter.reloadLastBookListRefreshTime()
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
        Logger.d(
            TAG, "onUIPositionChange isUnderTouch: $isUnderTouch, status: $status, " +
                    "over keep header: ${ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading}, " +
                    "over refresh: ${ptrIndicator?.isOverOffsetToRefresh}"
        )
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            refreshHeader.mtvRefresh.text = getString(R.string.release_to_refresh)
            refreshHeader.ivRefresh.rotation = REFRESHING_ROTATION
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
        presenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        Logger.d(TAG, "onUIReset")
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
                updateDotsHandler.postDelayed(this, DOT_TASK_DELAY)
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
}
