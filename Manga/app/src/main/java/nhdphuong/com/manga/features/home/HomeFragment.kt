package nhdphuong.com.manga.features.home

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import `in`.srain.cube.views.ptr.PtrUIHandler
import `in`.srain.cube.views.ptr.indicator.PtrIndicator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import nhdphuong.com.manga.Constants
import nhdphuong.com.manga.Constants.Companion.ACTION_SEARCH_QUERY_CHANGED
import nhdphuong.com.manga.Constants.Companion.BOOK_ID
import nhdphuong.com.manga.Constants.Companion.RECENT_DATA_UPDATED_ACTION
import nhdphuong.com.manga.Constants.Companion.SEARCH_INFO
import nhdphuong.com.manga.Constants.Companion.SELECTED_TAG
import nhdphuong.com.manga.Constants.Companion.TAG_SELECTED_ACTION
import nhdphuong.com.manga.NHentaiApp
import nhdphuong.com.manga.NotificationHelper
import nhdphuong.com.manga.R
import nhdphuong.com.manga.broadcastreceiver.BroadCastReceiverHelper
import nhdphuong.com.manga.views.adapters.BookAdapter
import nhdphuong.com.manga.data.entity.book.Book
import nhdphuong.com.manga.data.entity.book.SortOption
import nhdphuong.com.manga.enum.ErrorEnum
import nhdphuong.com.manga.features.NavigationRedirectActivity
import nhdphuong.com.manga.features.about.AboutUsActivity
import nhdphuong.com.manga.features.preview.BookPreviewActivity
import nhdphuong.com.manga.features.setting.SettingsActivity
import nhdphuong.com.manga.service.NetworkManager
import nhdphuong.com.manga.service.RecentFavoriteMigrationService
import nhdphuong.com.manga.views.MyGridLayoutManager
import nhdphuong.com.manga.views.adapters.BookAdapter.Companion.HOME_PREVIEW_BOOK
import nhdphuong.com.manga.views.adapters.BookAdapter.Companion.RECOMMEND_BOOK_WITH_ACTIONS
import nhdphuong.com.manga.views.becomeVisible
import nhdphuong.com.manga.views.becomeVisibleIf
import nhdphuong.com.manga.views.gone
import nhdphuong.com.manga.views.doOnGlobalLayout
import nhdphuong.com.manga.views.adapters.PaginationAdapter
import nhdphuong.com.manga.views.createLoadingDialog
import nhdphuong.com.manga.views.customs.MyButton
import nhdphuong.com.manga.views.customs.MyTextView
import nhdphuong.com.manga.views.showBookListRefreshingDialog
import nhdphuong.com.manga.views.showDoNotRecommendBookDialog
import nhdphuong.com.manga.views.showGoToPageDialog
import nhdphuong.com.manga.views.showTryAlternativeDomainsDialog
import javax.inject.Inject
import nhdphuong.com.manga.shared.ExternalRoutingViewModel
import nhdphuong.com.manga.views.uimodel.RoutingModel

/*
 * Created by nhdphuong on 3/16/18.
 */
class HomeFragment : Fragment(), HomeContract.View, PtrUIHandler, View.OnClickListener {

    private lateinit var homeListAdapter: BookAdapter
    private lateinit var homePaginationAdapter: PaginationAdapter
    private lateinit var homePresenter: HomeContract.Presenter
    private lateinit var loadingDialog: Dialog

    private var recommendAdapter: BookAdapter? = null

    private var searchResultTitle = ""
    private var lastUpdateTemplate = ""
    private var upgradeTitleTemplate = ""
    private var upgradeMessage = ""
    private var internetErrorLabel = ""
    private var dataParsingErrorLabel = ""
    private var timeOutErrorLabel = ""
    private var unknownErrorLabel = ""
    private var updated = ""
    private var releaseToRefresh = ""
    private var updating = ""
    private var pullDown = ""
    private var doNoRecommendMessageTemplate = ""

    private val updateDotsHandler: Handler = Handler(Looper.getMainLooper())
    private lateinit var btnFirst: ImageView
    private lateinit var btnJumpToPage: ImageView
    private lateinit var btnLast: ImageView
    private lateinit var clNavigation: ConstraintLayout
    private lateinit var clNothing: ConstraintLayout
    private lateinit var mbReload: MyButton
    private lateinit var mtvSearchResult: MyTextView
    private lateinit var nsvMainList: NestedScrollView
    private lateinit var refreshHeader: View
    private lateinit var rvMainList: RecyclerView
    private lateinit var rvPagination: RecyclerView
    private lateinit var srlPullToReload: PtrFrameLayout
    private lateinit var clUpgradePopup: ConstraintLayout
    private lateinit var ibUpgradePopupClose: ImageButton
    private lateinit var layoutSortOptions: HorizontalScrollView
    private lateinit var mtvPopularAllTime: MyTextView
    private lateinit var mtvPopularToday: MyTextView
    private lateinit var mtvPopularWeek: MyTextView
    private lateinit var mtvRecentOption: MyTextView
    private lateinit var mtvUpgradeTitle: MyTextView
    private lateinit var tvNothing: MyTextView
    private lateinit var upgradePopupPlaceHolder: View
    private lateinit var ivRefresh: ImageView
    private lateinit var mtvLastUpdate: MyTextView
    private lateinit var mtvRefresh: MyTextView
    private lateinit var pbRefresh: ProgressBar
    private lateinit var hsvRecommendList: HorizontalScrollView
    private lateinit var mtvRecommendBook: MyTextView
    private lateinit var rvRecommendList: RecyclerView

    @Inject
    lateinit var networkManager: NetworkManager

    private val activityResultCallback = ActivityResultCallback<ActivityResult> { result ->
        if (result?.resultCode == RESULT_OK) {
            when (result.data?.action) {
                RECENT_DATA_UPDATED_ACTION -> {
                    homePresenter.reloadRecentBooks()
                }
                TAG_SELECTED_ACTION -> {
                    result.data?.getStringExtra(SELECTED_TAG)?.let { selectedTag ->
                        if (selectedTag.isNotBlank()) {
                            val dataBundle = Bundle().apply {
                                putString(SELECTED_TAG, selectedTag)
                            }
                            BroadCastReceiverHelper.sendBroadCast(
                                context, ACTION_SEARCH_QUERY_CHANGED, dataBundle
                            )
                        }
                    }
                }
            }
        }
    }

    private val previewLauncher =
        registerForActivityResult(StartActivityForResult(), activityResultCallback)

    private var externalRoutingViewModel: ExternalRoutingViewModel? = null

    private fun setUpUI(rootView: View) {
        btnFirst = rootView.findViewById(R.id.btnFirst)
        btnJumpToPage = rootView.findViewById(R.id.btnJumpToPage)
        btnLast = rootView.findViewById(R.id.btnLast)
        clNavigation = rootView.findViewById(R.id.clNavigation)
        clNothing = rootView.findViewById(R.id.clNothing)
        mbReload = rootView.findViewById(R.id.mbReload)
        mtvSearchResult = rootView.findViewById(R.id.mtv_search_result)
        nsvMainList = rootView.findViewById(R.id.nsvMainList)
        refreshHeader = rootView.findViewById(R.id.refreshHeader)
        rvMainList = rootView.findViewById(R.id.rvMainList)
        rvPagination = rootView.findViewById(R.id.rvPagination)
        srlPullToReload = rootView.findViewById(R.id.srlPullToReload)
        clUpgradePopup = rootView.findViewById(R.id.clUpgradePopup)
        ibUpgradePopupClose = rootView.findViewById(R.id.ibUpgradePopupClose)
        layoutSortOptions = rootView.findViewById(R.id.layoutSortOptions)
        mtvPopularAllTime = rootView.findViewById(R.id.mtvPopularAllTime)
        mtvPopularToday = rootView.findViewById(R.id.mtvPopularToday)
        mtvPopularWeek = rootView.findViewById(R.id.mtvPopularWeek)
        mtvRecentOption = rootView.findViewById(R.id.mtvRecentOption)
        mtvUpgradeTitle = rootView.findViewById(R.id.mtvUpgradeTitle)
        tvNothing = rootView.findViewById(R.id.tvNothing)
        upgradePopupPlaceHolder = rootView.findViewById(R.id.upgradePopupPlaceHolder)
        hsvRecommendList = rootView.findViewById(R.id.hsvRecommendList)
        mtvRecommendBook = rootView.findViewById(R.id.mtvRecommendBook)
        rvRecommendList = rootView.findViewById(R.id.rvRecommendList)
        ivRefresh = refreshHeader.findViewById(R.id.ivRefresh)
        mtvLastUpdate = refreshHeader.findViewById(R.id.mtvLastUpdate)
        mtvRefresh = refreshHeader.findViewById(R.id.mtvRefresh)
        pbRefresh = refreshHeader.findViewById(R.id.pbRefresh)
    }

    override fun setPresenter(presenter: HomeContract.Presenter) {
        homePresenter = presenter
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as? FragmentActivity)?.let {
            externalRoutingViewModel =
                ViewModelProviders.of(it)[ExternalRoutingViewModel::class.java]
        }
    }

    override fun onDetach() {
        super.onDetach()
        externalRoutingViewModel = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_book_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            searchResultTitle = it.getString(R.string.search_result)
            lastUpdateTemplate = it.getString(R.string.last_update)
            upgradeTitleTemplate = it.getString(R.string.app_upgrade_notification_title)
            upgradeMessage = it.getString(R.string.app_upgrade_notification_message)
            internetErrorLabel = it.getString(R.string.internet_error)
            dataParsingErrorLabel = it.getString(R.string.library_error_data_parsing_label)
            timeOutErrorLabel = it.getString(R.string.library_error_time_out_label)
            unknownErrorLabel = it.getString(R.string.library_error_unknown_label)
            updated = it.getString(R.string.updated)
            releaseToRefresh = it.getString(R.string.release_to_refresh)
            updating = it.getString(R.string.updating)
            pullDown = it.getString(R.string.pull_down)
            doNoRecommendMessageTemplate = it.getString(R.string.do_not_recommend_book_accepted)
        }

        setUpUI(view)

        nsvMainList.overScrollMode = View.OVER_SCROLL_NEVER
        activity?.let {
            loadingDialog = it.createLoadingDialog()
            externalRoutingViewModel?.routingModel?.observe(it) { routing ->
                when (routing) {
                    is RoutingModel.BookQuerying -> handleExternalBookID(routing.bookId)
                    is RoutingModel.Search -> handleExternalSearchInfo(routing.searchInfo)
                }
            }
        }
        srlPullToReload.addPtrUIHandler(this)
        srlPullToReload.setPtrHandler(object : PtrHandler {
            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                homePresenter.reloadCurrentPage()
            }

            override fun checkCanDoRefresh(
                frame: PtrFrameLayout?,
                content: View?,
                header: View?
            ): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header)
            }
        })
        mbReload.setOnClickListener(this)
        mtvRecentOption.setOnClickListener(this)
        mtvPopularToday.setOnClickListener(this)
        mtvPopularWeek.setOnClickListener(this)
        mtvPopularAllTime.setOnClickListener(this)
        btnJumpToPage.setOnClickListener(this)
        btnFirst.setOnClickListener(this)
        btnLast.setOnClickListener(this)

        val bookId = arguments?.getString(BOOK_ID)
        val searchInfo = arguments?.getString(SEARCH_INFO)
        if (!bookId.isNullOrBlank()) {
            homePresenter.start()
            handleExternalBookID(bookId)
        } else if (!searchInfo.isNullOrBlank()) {
            handleExternalSearchInfo(searchInfo)
        } else {
            homePresenter.start()
            updateSearchInfo("")
        }
        mtvUpgradeTitle.setOnClickListener(this)
        ibUpgradePopupClose.setOnClickListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        externalRoutingViewModel?.routingModel?.removeObservers(this)
    }

    override fun onStart() {
        super.onStart()
        context?.let(networkManager::attach)
        networkManager.addNetworkAvailableTask {
            activity?.runOnUiThread {
                homePresenter.reloadIfEmpty()
                homePresenter.checkAndResumeBookDownloading()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        context?.let(networkManager::detach)
    }

    override fun onResume() {
        super.onResume()
        homePresenter.refreshAppVersion()
    }

    override fun onDestroy() {
        super.onDestroy()
        homePresenter.stop()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        val spanCount = if (isLandscape) LANDSCAPE_GRID_COLUMNS else GRID_COLUMNS
        val mainListLayoutManager = object : StaggeredGridLayoutManager(spanCount, VERTICAL) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        mainListLayoutManager.gapStrategy = StaggeredGridLayoutManager.HORIZONTAL
        rvMainList.layoutManager = mainListLayoutManager
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnFirst -> {
                homePresenter.jumToFirstPage()
                homePaginationAdapter.selectFirstPage()
                jumpTo(0)
            }

            R.id.btnLast -> {
                homePresenter.jumToLastPage()
                homePaginationAdapter.selectLastPage()
                jumpTo(homePaginationAdapter.itemCount - 1)
            }

            R.id.mbReload -> {
                homePresenter.reloadCurrentPage()
            }

            R.id.mtvRecentOption -> {
                layoutSortOptions.fullScroll(HorizontalScrollView.FOCUS_LEFT)
                homePresenter.updateSortOption(SortOption.Recent)
            }

            R.id.mtvPopularToday -> {
                homePresenter.updateSortOption(SortOption.PopularToday)
            }

            R.id.mtvPopularWeek -> {
                homePresenter.updateSortOption(SortOption.PopularWeek)
            }

            R.id.mtvPopularAllTime -> {
                layoutSortOptions.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
                homePresenter.updateSortOption(SortOption.PopularAllTime)
            }

            R.id.btnJumpToPage -> {
                activity?.run {
                    val maxPages = homePaginationAdapter.itemCount
                    if (maxPages > 1) {
                        showGoToPageDialog(1, maxPages, onOk = { page ->
                            val toJumpPage = page - 1
                            jumpTo(toJumpPage)
                            homePresenter.jumpToPage(toJumpPage.toLong())
                            homePaginationAdapter.jumpToIndex(toJumpPage)
                        })
                    }
                }
            }

            R.id.mtvUpgradeTitle -> {
                clUpgradePopup.gone()
                upgradePopupPlaceHolder.gone()
                homePresenter.setNewerVersionAcknowledged()
                context?.let {
                    AboutUsActivity.start(it)
                }
            }

            R.id.ibUpgradePopupClose -> {
                clUpgradePopup.gone()
                upgradePopupPlaceHolder.gone()
                homePresenter.setNewerVersionAcknowledged()
            }
        }
    }

    @SuppressLint("PrivateResource")
    override fun setUpHomeBookList(homeBookList: List<Book>) {
        homeListAdapter = BookAdapter(homeBookList, HOME_PREVIEW_BOOK, this::onBookSelected)
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

    override fun showRecommendBooks(bookList: List<Book>) {
        mtvRecommendBook.becomeVisible()
        hsvRecommendList.becomeVisible()
        context?.let {
            val gridLayoutManager = object : MyGridLayoutManager(it, bookList.size) {
                override fun isAutoMeasureEnabled(): Boolean {
                    return true
                }
            }

            rvRecommendList.layoutManager = gridLayoutManager

            recommendAdapter = BookAdapter(
                bookList,
                RECOMMEND_BOOK_WITH_ACTIONS,
                this::onRecommendedBookSelected,
                this::onBlockingBook,
                this::onFavoriteBookAdded,
                this::onFavoriteBookRemoved
            )
            rvRecommendList.adapter = recommendAdapter
        }
    }

    override fun refreshRecommendBooks() {
        recommendAdapter?.notifyDataSetChanged()
    }

    override fun hideRecommendBooks() {
        mtvRecommendBook.gone()
        hsvRecommendList.gone()
    }

    override fun refreshHomeBookList() {
        homeListAdapter.notifyDataSetChanged()
        homePresenter.saveLastBookListRefreshTime()
        rvMainList.post {
            rvMainList.smoothScrollBy(0, 0)
        }
        homePresenter.reloadRecentBooks()
    }

    override fun refreshHomePagination(pageCount: Long, currentFocusedIndex: Int) {
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
        val updateNavigationButtons = {
            mainPagination.post {
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                val showJumpToFirstButton = firstVisibleItemPosition > 0
                val showJumpToLastButton = lastVisibleItemPosition < pageCount - 1
                if (firstVisibleItemPosition >= 0) {
                    btnFirst.becomeVisibleIf(showJumpToFirstButton)
                }
                if (lastVisibleItemPosition >= 0) {
                    btnLast.becomeVisibleIf(showJumpToLastButton)
                }
                btnJumpToPage.becomeVisibleIf(showJumpToFirstButton || showJumpToLastButton)
            }
        }
        mainPagination.doOnGlobalLayout {
            updateNavigationButtons.invoke()
            homePaginationAdapter.jumpToIndex(currentFocusedIndex)
        }
        mainPagination.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                updateNavigationButtons.invoke()
            }
        })
    }

    override fun showLastBookListRefreshTime(lastRefreshTimeStamp: String) {
        val lastRefresh = String.format(lastUpdateTemplate, lastRefreshTimeStamp)
        mtvLastUpdate.text = lastRefresh
    }

    override fun showNothingView() {
        clNothing.becomeVisible()
    }

    override fun hideNothingView() {
        clNothing.gone()
    }

    override fun enableSortOption(sortOption: SortOption) {
        mtvRecentOption.isActivated = sortOption == SortOption.Recent
        mtvPopularToday.isActivated = sortOption == SortOption.PopularToday
        mtvPopularWeek.isActivated = sortOption == SortOption.PopularWeek
        mtvPopularAllTime.isActivated = sortOption == SortOption.PopularAllTime
    }

    override fun showSortOptionList() {
        layoutSortOptions.becomeVisible()
    }

    override fun hideSortOptionList() {
        layoutSortOptions.gone()
    }

    override fun showRefreshingDialog() {
        activity?.showBookListRefreshingDialog()
    }

    override fun showFavoriteBooks(favoriteList: List<String>) {
        homeListAdapter.setFavoriteList(favoriteList)
    }

    override fun showFavoriteRecommendedBooks(favoriteList: List<String>) {
        recommendAdapter?.setFavoriteList(favoriteList)
    }

    override fun showRecentBooks(recentList: List<String>) {
        homeListAdapter.setRecentList(recentList)
    }

    override fun changeSearchInfo(data: String) {
        updateSearchInfo(data)
    }

    override fun showBookPreview(book: Book) {
        BookPreviewActivity.start(this, previewLauncher, book)
    }

    override fun startUpdateTagsService() {
        NHentaiApp.instance.startUpdateTagsService()
    }

    override fun showUpgradeNotification(latestVersionCode: String) {
        clUpgradePopup.becomeVisible()
        upgradePopupPlaceHolder.becomeVisible()
        clUpgradePopup.postDelayed({
            clUpgradePopup.gone()
            upgradePopupPlaceHolder.gone()
        }, APP_UPGRADE_TIME_OUT)
        val title = String.format(upgradeTitleTemplate, latestVersionCode)
        val message = upgradeMessage
        activity?.let {
            val notificationIntent = Intent(it, NavigationRedirectActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                it, 0,
                notificationIntent, Intent.FILL_IN_ACTION
            )
            NotificationHelper.sendNotification(
                title,
                NotificationCompat.PRIORITY_DEFAULT,
                message,
                true,
                Constants.APP_UPGRADE_NOTIFICATION_ID,
                pendingIntent
            )
        }
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

    override fun finishRefreshing() {
        srlPullToReload.postDelayed({
            srlPullToReload.refreshComplete()
        }, REFRESH_COMPLETE_DURATION)
    }

    override fun showAlternativeDomainsQuestion() {
        activity?.let {
            it.showTryAlternativeDomainsDialog(onOk = {
                homePresenter.checkedOutAlternativeDomains()
                SettingsActivity.start(it)
            })
        }
    }

    override fun startRecentFavoriteMigration() {
        context?.let {
            RecentFavoriteMigrationService.enqueueWork(it)
        }
    }

    fun changeSearchInputted(data: String) {
        homePresenter.updateSortOption(SortOption.Recent)
        homePresenter.updateSearchData(data)
        hideSortOptionList()
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
        mtvRefresh.text = updated
        homePresenter.saveLastBookListRefreshTime()
        homePresenter.reloadLastBookListRefreshTime()
        ivRefresh.rotation = 0F
        ivRefresh.becomeVisible()
        pbRefresh.gone()
        updateDotsHandler.removeCallbacksAndMessages(null)
    }

    override fun onUIPositionChange(
        frame: PtrFrameLayout?,
        isUnderTouch: Boolean,
        status: Byte,
        ptrIndicator: PtrIndicator?
    ) {
        if (ptrIndicator?.isOverOffsetToKeepHeaderWhileLoading == true) {
            mtvRefresh.text = releaseToRefresh
            ivRefresh.rotation = REFRESH_HEADER_ANGEL
        }
    }

    override fun onUIRefreshBegin(frame: PtrFrameLayout?) {
        ivRefresh.gone()
        pbRefresh.becomeVisible()
        mtvRefresh.text = String.format(updating, "")
        runUpdateDotsTask()

    }

    override fun onUIRefreshPrepare(frame: PtrFrameLayout?) {
        homePresenter.reloadLastBookListRefreshTime()
    }

    override fun onUIReset(frame: PtrFrameLayout?) {
        mtvRefresh.text = pullDown
    }

    private fun jumpTo(pageNumber: Int) {
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            rvPagination.scrollToPosition(pageNumber)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun runUpdateDotsTask() {
        var currentPos = 0
        val updateDotsTask = {
            val dotsArray = resources.getStringArray(R.array.dots)
            mtvRefresh.text =
                String.format(updating, dotsArray[currentPos])
            if (currentPos < dotsArray.size - 1) currentPos++ else currentPos = 0
        }
        val runnable = object : Runnable {
            override fun run() {
                updateDotsTask()
                updateDotsHandler.postDelayed(this, DOTS_UPDATE_INTERVAL)
            }
        }
        updateDotsHandler.post(runnable)
    }

    private fun updateSearchInfo(data: String) {
        mtvSearchResult.run {
            text = String.format(searchResultTitle, data)
            becomeVisibleIf(data.isNotBlank())
        }
    }

    private fun onBookSelected(book: Book) {
        BookPreviewActivity.start(this, previewLauncher, book)
    }

    private fun onRecommendedBookSelected(book: Book) {
        homePresenter.checkOutRecommendedBook(book.bookId)
        BookPreviewActivity.start(this@HomeFragment, previewLauncher, book)
    }

    private fun onBlockingBook(bookId: String) {
        activity?.run {
            showDoNotRecommendBookDialog(bookId, onOk = {
                homePresenter.doNoRecommendBook(bookId)
                val acceptedMessage =
                    String.format(doNoRecommendMessageTemplate, bookId)
                Toast.makeText(this, acceptedMessage, Toast.LENGTH_LONG).show()
            })
        }
    }

    private fun onFavoriteBookAdded(book: Book) {
        homePresenter.addFavoriteRecommendedBook(book)
    }

    private fun onFavoriteBookRemoved(book: Book) {
        homePresenter.removeFavoriteRecommendedBook(book)
    }

    private fun handleExternalBookID(bookId: String) {
        homePresenter.openBook(bookId)
        updateSearchInfo("")
    }

    private fun handleExternalSearchInfo(searchInfo: String) {
        updateSearchInfo(searchInfo)
        changeSearchInputted(searchInfo)
    }

    companion object {
        private const val GRID_COLUMNS = 2
        private const val LANDSCAPE_GRID_COLUMNS = 3
        private const val DOTS_UPDATE_INTERVAL = 500L
        private const val REFRESH_COMPLETE_DURATION = 800L
        private const val REFRESH_HEADER_ANGEL = 180F
        private const val APP_UPGRADE_TIME_OUT = 2 * 60 * 1000L
    }
}
